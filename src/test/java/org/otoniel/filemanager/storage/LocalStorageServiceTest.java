package org.otoniel.filemanager.storage;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.otoniel.resources.models.FileMetadata;
import org.otoniel.resources.storage.LocalStorageService;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    LocalStorageService localStorageService;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService();
        localStorageService.setFolder(tempDir.toString());
    }

    @Test
    void create() {
        String fileName = "fileName.txt";

        final FileMetadata fileMetadata = localStorageService.create(new ByteArrayInputStream("test".getBytes()),
                fileName);

        assertEquals(fileName, fileMetadata.getOriginalFileName());
        assertNotNull(fileMetadata.getFileName());
        assertNotNull(fileMetadata.getChecksum());
        assertEquals(4L, fileMetadata.getSize());
    }

    @Test
    void readAfterCreate() {
        String fileName = "fileName.txt";
        byte[] fileContent = "test".getBytes();
        FileMetadata fileMetadata = localStorageService.create(new ByteArrayInputStream(fileContent), fileName);

        final byte[] readBytes = localStorageService.read(fileMetadata.getFileName());

        assertTrue(Arrays.equals(readBytes, fileContent));
    }

    @Test
    void removeNonExisting() {
        assertThrows(IllegalStateException.class, () -> localStorageService.remove("non_existing"));
    }

    @Test
    void removeExisting() {
        String fileName = "fileName.txt";
        byte[] fileContent = "test".getBytes();
        FileMetadata fileMetadata = localStorageService.create(new ByteArrayInputStream(fileContent), fileName);

        localStorageService.remove(fileMetadata.getFileName());
    }


}
