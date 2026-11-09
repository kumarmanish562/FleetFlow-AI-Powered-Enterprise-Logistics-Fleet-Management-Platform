package com.fleetflow.ai.knowledge.reader;

import com.fleetflow.ai.knowledge.model.DocumentType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class KnowledgeDocumentReader {
    private final int maxCharacters;
    private final Tika tika = new Tika();

    public KnowledgeDocumentReader(@Value("${fleetflow.knowledge.max-extracted-characters:1000000}") int maxCharacters) {
        this.maxCharacters = Math.max(1000, Math.min(maxCharacters, 2_000_000));
    }

    public List<PageText> read(Path file) {
        String name = file.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        try {
            if (name.endsWith(".pdf")) return readPdf(file);
            String text;
            if (name.endsWith(".txt")) text = Files.readString(file);
            else try (var stream = Files.newInputStream(file)) { text = tika.parseToString(stream, new Metadata(), maxCharacters + 1); }
            text = clean(text);
            if (text.isBlank() || text.length() > maxCharacters) throw new IllegalArgumentException("Document has no usable text or exceeds extraction limit");
            return List.of(new PageText(null, text));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not extract document text", ex);
        }
    }

    private List<PageText> readPdf(Path file) throws IOException {
        List<PageText> pages = new ArrayList<>();
        int total = 0;
        try (PDDocument pdf = Loader.loadPDF(file.toFile())) {
            if (pdf.isEncrypted()) throw new IllegalArgumentException("Encrypted PDFs are unsupported");
            if (pdf.getNumberOfPages() > 1000) throw new IllegalArgumentException("PDF has too many pages");
            PDFTextStripper stripper = new PDFTextStripper();
            for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
                stripper.setStartPage(page); stripper.setEndPage(page);
                String text = clean(stripper.getText(pdf));
                total += text.length();
                if (total > maxCharacters) throw new IllegalArgumentException("Document exceeds extraction limit");
                if (!text.isBlank()) pages.add(new PageText(page, text));
            }
        }
        if (pages.isEmpty()) throw new IllegalArgumentException("PDF has no extractable text");
        return pages;
    }

    private String clean(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n')
                .replaceAll("[\\t ]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
    }

    public record PageText(Integer pageNumber, String text) {}
}
