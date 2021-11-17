package org.otoniel.resources.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.otoniel.resources.models.FileMetadata;

@ApplicationScoped
public class LocalStorageService implements StorageService {

    private static final Logger LOG = Logger.getLogger(LocalStorageService.class);

    @ConfigProperty(name="filemanager.folder", defaultValue = "/tmp")
    String folder;

    public boolean isFolderReady() {
        Path path = Path.of(folder);
        return Files.exists(path) && Files.isWritable(path);
    }

    public FileMetadata create(InputStream inputStream, String originalFilename) {
        if (!isFolderReady()) {
            LOG.warn("Folder doesn't exist or is not writable");
            throw new IllegalStateException("Internal folder not ready for writes");
        }

        String internalFileName = UUID.randomUUID().toString();
        File targetFile = new File(folder, internalFileName);

        try {
            FileUtils.copyInputStreamToFile(inputStream, targetFile);
            byte[] bytes = Files.readAllBytes(targetFile.toPath());

            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileName(internalFileName);
            fileMetadata.setOriginalFileName(originalFilename);
            fileMetadata.setSize(FileUtils.sizeOf(targetFile));
            fileMetadata.setChecksum(DigestUtils.sha256Hex(bytes));
            fileMetadata.setVersion(1);

            return fileMetadata;
        } catch (IOException e) {
            LOG.warn("Error to write", e);
            throw new IllegalStateException("Fail to write file", e);
        }


    }

    @Override
    public byte[] read(String fileName) {
        try {
            File targetFile = new File(folder, fileName);
            return  FileUtils.readFileToByteArray(targetFile);
        } catch (IOException ex) {
            LOG.warn("Error to read", ex);
            throw new IllegalStateException("Fail to read file", ex);
        }
    }

    @Override
    public void remove(String fileName) {
        try {
            File targetFile = new File(folder, fileName);
            FileUtils.delete(targetFile);
        } catch (IOException e) {
            LOG.warn("Error to delete", e);
            throw new IllegalStateException("Fail to delete file", e);
        }
    }

    @Override
    public boolean canBeRemoved(String fileName) {
        File targetFile = new File(folder, fileName);
        return !FileUtils.isDirectory(targetFile) && targetFile.canWrite();
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

}
