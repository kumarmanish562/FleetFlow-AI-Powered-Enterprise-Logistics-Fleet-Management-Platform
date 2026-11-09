package com.fleetflow.ai.knowledge;

import com.fleetflow.ai.knowledge.reader.KnowledgeDocumentReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeDocumentReaderTest {
    private final Path root = Path.of("target", "ai-reader-tests").toAbsolutePath();
    @BeforeEach void createDirectory() throws Exception { Files.createDirectories(root); }
    private final KnowledgeDocumentReader reader = new KnowledgeDocumentReader(10_000);

    @Test
    void extractsUtf8Text() throws Exception {
        Path path = root.resolve("manual.txt"); Files.writeString(path, "Inspect tyres before departure.");
        assertEquals("Inspect tyres before departure.", reader.read(path).getFirst().text());
        assertNull(reader.read(path).getFirst().pageNumber());
    }

    @Test
    void extractsPdfWithPageNumber() throws Exception {
        Path path = root.resolve("manual.pdf");
        try (PDDocument pdf = new PDDocument()) {
            PDPage page = new PDPage(); pdf.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(pdf, page)) {
                content.beginText(); content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(50, 700); content.showText("Report a breakdown to dispatch."); content.endText();
            }
            pdf.save(path.toFile());
        }
        var pages = reader.read(path);
        assertEquals(1, pages.size());
        assertEquals(1, pages.getFirst().pageNumber());
        assertTrue(pages.getFirst().text().contains("Report a breakdown"));
    }

    @Test
    void extractsDocx() throws Exception {
        Path path = root.resolve("manual.docx");
        try (XWPFDocument document = new XWPFDocument()) {
            document.createParagraph().createRun().setText("Use the safety checklist.");
            try (var output = Files.newOutputStream(path)) { document.write(output); }
        }
        assertTrue(reader.read(path).getFirst().text().contains("Use the safety checklist"));
    }
}
