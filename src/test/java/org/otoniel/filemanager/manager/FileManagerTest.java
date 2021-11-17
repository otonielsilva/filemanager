package org.otoniel.filemanager.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.graalvm.collections.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.otoniel.resources.repo.FileRepository;
import org.otoniel.resources.manager.FileManager;
import org.otoniel.resources.models.FileData;
import org.otoniel.resources.models.FileManagerException;
import org.otoniel.resources.models.FileManagerException.ErrorType;
import org.otoniel.resources.models.FileObject;
import org.otoniel.resources.models.FileVersionData;
import org.otoniel.resources.models.FileMetadata;
import org.otoniel.resources.storage.StorageService;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class FileManagerTest {

    private StorageService storage;
    private FileRepository fileRepository;
    private ArgumentCaptor<FileData> argumentCaptor;

    private FileManager fileManager;

    @BeforeEach
    void setUp() {
        storage = mock(StorageService.class);
        fileRepository = mock(FileRepository.class);
        argumentCaptor = ArgumentCaptor.forClass(FileData.class);

        fileManager = new FileManager(fileRepository, storage);
    }

    @Test
    @DisplayName("Test if file is stored and save successfully")
    public void create_uploadSimpleFile_shouldReturnFileCreated() {
        when(storage.create(any(InputStream.class), eq("fileName.jpg"))).thenReturn(givenFileMetaData());

        final FileObject fileObject = fileManager.create(new ByteArrayInputStream("asdfasdf".getBytes()),
                "fileName.jpg");

        assertNotNull(fileObject);
        assertEquals("fileName.jpg", fileObject.getOriginalFileName());
        assertEquals(1L, fileObject.getLastVersion());

        verify(fileRepository).persist(argumentCaptor.capture());
        FileData fileData = argumentCaptor.getValue();
        assertEquals("fileName.jpg", fileData.getOriginalFileName());
        assertEquals(1L, fileData.getLastVersion());
        assertEquals(1L, fileData.getFileVersions().stream().count());
    }

    @Test
    @DisplayName("Test if exception is thrown, nothing should be saved and return error")
    public void create_throwAnException_shouldReturnFileCreated() {
        when(storage.create(any(InputStream.class), eq("fileName.jpg"))).thenThrow(IllegalStateException.class);

        assertThrows(FileManagerException.class,
                () -> fileManager.create(new ByteArrayInputStream("asdfasdf".getBytes()), "fileName.jpg"));

        verify(fileRepository, Mockito.never()).persist(any(FileData.class));
    }

    @Test
    @DisplayName("Test read file without version as parameter")
    public void read_fileExists_shouldReturnFileCreated() {
        byte[] fileTest = "Test".getBytes();
        when(storage.read(anyString())).thenReturn(fileTest);
        when(fileRepository.findById(anyString())).thenReturn(Optional.of(givenFileData()));

        final Pair<String, byte[]> filePair = fileManager.readFile("file_id", null);

        assertEquals("fileName.jpg", filePair.getLeft());
        assertEquals(fileTest, filePair.getRight());
    }

    @Test
    @DisplayName("Test read VALID file and version as parameter")
    public void read_fileAndVersionExists_shouldReturnFileCreated() {
        byte[] fileTest = "Test".getBytes();
        when(storage.read(anyString())).thenReturn(fileTest);
        when(fileRepository.findById(anyString())).thenReturn(Optional.of(givenFileData()));

        final Pair<String, byte[]> filePair = fileManager.readFile("file_id", "1");

        assertEquals("fileName.jpg", filePair.getLeft());
        assertEquals(fileTest, filePair.getRight());
    }

    @Test
    @DisplayName("Test read with error in Storage")
    public void read_storageError_shouldReturnFileCreated() {
        when(storage.read(anyString())).then(invocation -> {
            throw new IOException();
        });
        when(fileRepository.findById(anyString())).thenReturn(Optional.of(givenFileData()));

        final FileManagerException exception = assertThrows(FileManagerException.class,
                () -> fileManager.readFile("file_id", "1"));

        assertEquals(ErrorType.UNKNOWN, exception.getType());
    }

    @Test
    @DisplayName("Test read INVALID file and version as parameter")
    public void read_fileAndVersionDontExists_shouldReturnFileCreated() {
        when(fileRepository.findById(anyString())).thenReturn(Optional.empty());

        final FileManagerException exception = assertThrows(FileManagerException.class,
                () -> fileManager.readFile("file_id", "20"));

        assertEquals(ErrorType.NOT_FOUND, exception.getType());
    }

    @Test
    @DisplayName("Test update INVALID file")
    public void update_fileDontExists_shouldReturnFileCreated() {
        when(fileRepository.findById(anyString())).thenReturn(Optional.empty());

        final FileManagerException exception = assertThrows(FileManagerException.class,
                () -> fileManager.update("file_id", new ByteArrayInputStream("Test".getBytes()), "fileName2.jpg"));

        assertEquals(ErrorType.NOT_FOUND, exception.getType());
    }

    @Test
    @DisplayName("Test update VALID file")
    public void update_fileExists_shouldReturnFileCreated() {
        final String newFileName = "fileName2.jpg";
        final FileMetadata fileMetadata = givenFileMetaData();
        fileMetadata.setOriginalFileName(newFileName);
        when(storage.create(any(InputStream.class), anyString())).thenReturn(fileMetadata);
        when(fileRepository.findById(anyString())).thenReturn(Optional.of(givenFileData()));

        final FileObject fileObject = fileManager.update("file_id", new ByteArrayInputStream("Test".getBytes()),
                newFileName);

        assertNotNull(fileObject);
        assertEquals(newFileName, fileObject.getOriginalFileName());
        assertTrue(fileObject.getUpdatedAt().isAfter(fileObject.getCreatedAt()));
        assertEquals(fileObject.getLastVersion(), Integer.valueOf(2));

        verify(fileRepository).persist(argumentCaptor.capture());
        FileData fileData = argumentCaptor.getValue();
        assertEquals("fileName2.jpg", fileData.getOriginalFileName());
        assertEquals(2L, fileData.getLastVersion());
        assertEquals(2L, fileData.getFileVersions().stream().count());
    }


    public FileMetadata givenFileMetaData() {
        final FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileName(UUID.randomUUID().toString());
        fileMetadata.setOriginalFileName("fileName.jpg");
        fileMetadata.setChecksum("dummyCheckSUM");
        fileMetadata.setSize(12321L);
        return fileMetadata;
    }

    public FileData givenFileData() {

        LocalDateTime now = LocalDateTime.now();
        final FileData fileData = new FileData();
        fileData.setLastVersion(1);
        fileData.setFileName("storage_fileName");
        fileData.setOriginalFileName("fileName.jpg");
        fileData.setSize(12321L);
        fileData.setChecksum("dummyCheckSUM");
        fileData.setCreatedAt(now);
        fileData.setUpdatedAt(now);

        FileVersionData fileVersionData = new FileVersionData();
        fileVersionData.setVersion(1);
        fileVersionData.setFileName("storage_fileName");
        fileVersionData.setOriginalFileName("fileName.jpg");
        fileVersionData.setSize(12321L);
        fileVersionData.setChecksum("dummyCheckSUM");

        fileData.addFileVersion(fileVersionData);

        return fileData;
    }

}
