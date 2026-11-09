package com.fleetflow.ai.knowledge.storage;

import com.fleetflow.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipInputStream;

@Service
public class KnowledgeFileStorage {
    private final Path root;
    private final long maxFileSize;

    public KnowledgeFileStorage(@Value("${fleetflow.knowledge.storage-location:./storage/knowledge}") String root,
                                @Value("${fleetflow.knowledge.max-file-size:20971520}") long maxFileSize) {
        this.root = Path.of(root).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
    }

    public StoredFile store(MultipartFile file, UUID organizationId, UUID documentId) {
        if (file == null || file.isEmpty() || file.getSize() > maxFileSize) {
            throw new BadRequestException("Document must be non-empty and at most " + maxFileSize + " bytes");
        }
        String original = file.getOriginalFilename();
        if (original == null) throw new BadRequestException("Filename is required");
        String filename = original.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1).trim();
        if (filename.isEmpty() || filename.length() > 255 || filename.indexOf('\0') >= 0) {
            throw new BadRequestException("Invalid filename");
        }
        String extension = filename.substring(Math.max(0, filename.lastIndexOf('.') + 1)).toLowerCase(Locale.ROOT);
        if (!extension.equals("pdf") && !extension.equals("docx") && !extension.equals("txt")) {
            throw new BadRequestException("Only PDF, DOCX, and TXT files are supported");
        }
        String declaredType = file.getContentType();
        if (declaredType != null && !declaredType.isBlank() && !declaredType.equals("application/octet-stream")) {
            boolean matches = switch (extension) {
                case "pdf" -> declaredType.equals("application/pdf");
                case "docx" -> declaredType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        || declaredType.equals("application/zip");
                default -> declaredType.equals("text/plain");
            };
            if (!matches) throw new BadRequestException("Declared content type does not match the file extension");
        }
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length == 0 || bytes.length > maxFileSize) throw new BadRequestException("Invalid document size");
            verifySignature(extension, bytes);
            String storageKey = organizationId + "/" + documentId + "." + extension;
            Path destination = resolve(storageKey);
            Files.createDirectories(destination.getParent());
            Files.write(destination, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            String checksum = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            return new StoredFile(filename, storageKey, checksum, bytes.length);
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not store knowledge document", ex);
        }
    }

    public Path resolve(String storageKey) {
        Path path = root.resolve(storageKey).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Invalid storage key");
        return path;
    }

    public void delete(String storageKey) {
        try { Files.deleteIfExists(resolve(storageKey)); }
        catch (IOException ex) { throw new IllegalStateException("Could not delete knowledge document", ex); }
    }

    private void verifySignature(String extension, byte[] bytes) {
        if (extension.equals("pdf")) {
            if (bytes.length < 5 || !new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")) {
                throw new BadRequestException("File content is not a PDF");
            }
        } else if (extension.equals("docx")) {
            if (bytes.length < 4 || bytes[0] != 'P' || bytes[1] != 'K') {
                throw new BadRequestException("File content is not a DOCX");
            }
            boolean wordDocument = false;
            boolean contentTypes = false;
            try (ZipInputStream zip = new ZipInputStream(new java.io.ByteArrayInputStream(bytes))) {
                java.util.zip.ZipEntry entry;
                int count = 0;
                long totalExpanded = 0;
                byte[] buffer = new byte[8192];
                while ((entry = zip.getNextEntry()) != null) {
                    if (++count > 5000) throw new BadRequestException("DOCX has too many entries");
                    if (entry.getName().equals("word/document.xml")) wordDocument = true;
                    if (entry.getName().equals("[Content_Types].xml")) contentTypes = true;
                    long expanded = 0;
                    int read;
                    while ((read = zip.read(buffer)) != -1) {
                        expanded += read; totalExpanded += read;
                        if (expanded > 5_000_000 || totalExpanded > 50_000_000) {
                            throw new BadRequestException("DOCX expands beyond the safe limit");
                        }
                    }
                    zip.closeEntry();
                }
            } catch (IOException ex) { throw new BadRequestException("Malformed DOCX file"); }
            if (!wordDocument || !contentTypes) throw new BadRequestException("File content is not a DOCX");
        } else {
            try {
                String text = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(bytes)).toString();
                if (text.indexOf('\0') >= 0) throw new BadRequestException("TXT file contains binary content");
            } catch (CharacterCodingException ex) { throw new BadRequestException("TXT file must use UTF-8"); }
        }
    }

    public record StoredFile(String filename, String storageKey, String checksum, long size) {}
}
