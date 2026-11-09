package com.fleetflow.ai.knowledge;

import com.fleetflow.ai.knowledge.storage.KnowledgeFileStorage;
import com.fleetflow.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeFileStorageTest {
    private final Path root = Path.of("target", "ai-storage-tests").toAbsolutePath();
    @BeforeEach void createDirectory() throws Exception { Files.createDirectories(root); }

    @Test
    void storesPrivateTxtUnderGeneratedKey() throws Exception {
        KnowledgeFileStorage storage = new KnowledgeFileStorage(root.toString(), 1000);
        var file = new MockMultipartFile("file", "../../maintenance.txt", "text/plain", "Check tyres".getBytes());
        UUID organization = UUID.randomUUID(); UUID document = UUID.randomUUID();

        var stored = storage.store(file, organization, document);

        assertEquals("maintenance.txt", stored.filename());
        assertEquals(organization + "/" + document + ".txt", stored.storageKey());
        assertEquals("Check tyres", Files.readString(storage.resolve(stored.storageKey())));
        assertEquals(64, stored.checksum().length());
    }

    @Test
    void rejectsMismatchedPdfSignatureAndBinaryTxt() {
        KnowledgeFileStorage storage = new KnowledgeFileStorage(root.toString(), 1000);
        assertThrows(BadRequestException.class, () -> storage.store(
                new MockMultipartFile("file", "fake.pdf", "application/pdf", "hello".getBytes()), UUID.randomUUID(), UUID.randomUUID()));
        assertThrows(BadRequestException.class, () -> storage.store(
                new MockMultipartFile("file", "binary.txt", "text/plain", new byte[]{0, 1, 2}), UUID.randomUUID(), UUID.randomUUID()));
    }
}
