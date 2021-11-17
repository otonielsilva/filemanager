package org.otoniel.resources.manager;

import static io.smallrye.openapi.runtime.util.StringUtil.isNotEmpty;
import static java.lang.String.format;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.NotFoundException;

import org.graalvm.collections.Pair;
import org.jboss.logging.Logger;
import org.otoniel.resources.models.FileData;
import org.otoniel.resources.repo.FileRepository;
import org.otoniel.resources.models.FileManagerException;
import org.otoniel.resources.models.FileManagerException.ErrorType;
import org.otoniel.resources.models.FileObject;
import org.otoniel.resources.models.FileVersionData;
import org.otoniel.resources.models.FileMetadata;
import org.otoniel.resources.storage.StorageService;

@ApplicationScoped
public class FileManager {

    private static final Logger LOG = Logger.getLogger(FileManager.class);

    private final FileRepository fileRepository;
    private final StorageService fileStorageService;

    @Inject
    public FileManager(FileRepository fileRepository, StorageService storageService) {
        this.fileRepository = fileRepository;
        this.fileStorageService = storageService;
    }

    @Transactional(TxType.REQUIRED)
    public FileObject create(InputStream inputStream, String fileName) {
        try {
            FileMetadata fileMetadata = fileStorageService.create(inputStream, fileName);
            LOG.info(format("File %s stored. ", fileMetadata));

            FileVersionData fileVersion = new FileVersionData();
            fileVersion.setFileName(fileMetadata.getFileName());
            fileVersion.setOriginalFileName(fileMetadata.getOriginalFileName());
            fileVersion.setChecksum(fileMetadata.getChecksum());
            fileVersion.setSize(fileMetadata.getSize());
            fileVersion.setVersion(1);

            FileData fileObject = new FileData();
            fileObject.setOriginalFileName(fileMetadata.getOriginalFileName());
            fileObject.setFileName(fileMetadata.getFileName());
            fileObject.addFileVersion(fileVersion);
            fileObject.setLastVersion(fileVersion.getVersion());
            fileObject.setChecksum(fileMetadata.getChecksum());
            fileObject.setSize(fileMetadata.getSize());
            fileObject.setCreatedAt(LocalDateTime.now());
            fileObject.setUpdatedAt(LocalDateTime.now());

            fileVersion.setFileObject(fileObject);

            fileRepository.persist(fileObject);

            LOG.info(format("File saved successfully with ID=%s ", fileObject.getId()));
            return mapTo(fileObject);
        } catch (Exception ex) {
            LOG.warn("Error during creating of file", ex);
            throw new FileManagerException(ErrorType.UNKNOWN, "Error to create file");
        }
    }

    public FileObject get(String id) {
        Optional<FileData> fileData =  fileRepository.findById(id);
        if (fileData.isEmpty()) {
            throw new NotFoundException(format("File not found with %s", id));
        }
        return fileData.map(this::mapTo).get();
    }

    /**
     * Read file content
     * @param fileId Internal File ID
     * @param version Version of file
     * @return Return Pair of file name and its content
     */
    public Pair<String, byte[]> readFile(String fileId, String version) {
        try {
            final Optional<FileData> file = fileRepository.findById(fileId);
            if (file.isPresent()) {
                // Return the desired version
                if (isNotEmpty(version)) {
                    final Optional<FileVersionData> fileVersionDataOptional = file.get().getFileVersions().stream()
                            .filter(fileVersionData -> String.valueOf(fileVersionData.getVersion()).equals(version))
                            .findFirst();
                    if (fileVersionDataOptional.isEmpty()) {
                        throw new FileManagerException(ErrorType.NOT_FOUND,
                                format("File not found with id = %s and version = %s ", fileId, version));

                    }
                    LOG.info(format("Requesting file: %s with version: %s", fileId, version));
                    return Pair.create(fileVersionDataOptional.get().getOriginalFileName(),
                            fileStorageService.read(fileVersionDataOptional.get().getFileName()));
                }

                // If version not provided, return the latest.
                return Pair.create(file.get().getOriginalFileName(), fileStorageService.read(file.get().getFileName()));
            }
            throw new FileManagerException(ErrorType.NOT_FOUND, format("File not found with %s", fileId));
        } catch (FileManagerException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.warn("Error during reading of file", ex);
            throw new FileManagerException(ErrorType.UNKNOWN, "Error to read file");
        }

    }

    public List<FileObject> list(Integer pageNumber, Integer pageSize) {
        LOG.info(format("Getting pageNumber=%d - pageSize=%d ", pageNumber, pageSize));

        return fileRepository.findAll(pageNumber, pageSize)
                .stream()
                .map(this::mapTo)
                .collect(Collectors.toList());
    }

    private FileObject mapTo(FileData fileData) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileData.getId());
        fileObject.setOriginalFileName(fileData.getOriginalFileName());
        fileObject.setChecksum(fileData.getChecksum());
        fileObject.setSize(fileData.getSize());
        fileObject.setLastVersion(fileData.getLastVersion());
        fileObject.setCreatedAt(fileData.getCreatedAt());
        fileObject.setUpdatedAt(fileData.getUpdatedAt());
        return fileObject;
    }

    @Transactional(TxType.REQUIRED)
    public FileObject update(String fileId, InputStream inputStream, String fileName) {
        try {
            Optional<FileData> fileDataOptional = fileRepository.findById(fileId);
            if (fileDataOptional.isEmpty()) {
                throw new FileManagerException(ErrorType.NOT_FOUND, format("File not found with %s", fileId));
            }
            FileData fileData = fileDataOptional.get();

            FileMetadata fileMetadata = fileStorageService.create(inputStream, fileName);
            LOG.info(format("File %s stored. ", fileMetadata));

            FileVersionData fileVersion = new FileVersionData();
            fileVersion.setFileName(fileMetadata.getFileName());
            fileVersion.setOriginalFileName(fileMetadata.getOriginalFileName());
            fileVersion.setChecksum(fileMetadata.getChecksum());
            fileVersion.setSize(fileMetadata.getSize());
            fileVersion.setVersion(fileData.getLastVersion() + 1);

            fileData.setOriginalFileName(fileMetadata.getOriginalFileName());
            fileData.setFileName(fileMetadata.getFileName());
            fileData.setChecksum(fileMetadata.getChecksum());
            fileData.setSize(fileMetadata.getSize());

            fileData.setLastVersion(fileData.getLastVersion() + 1);
            fileData.addFileVersion(fileVersion);
            fileData.setUpdatedAt(LocalDateTime.now());
            fileVersion.setFileObject(fileData);

            fileRepository.persist(fileData);

            LOG.info(format("New file version saved successfully with ID=%s ", fileData.getId()));
            return mapTo(fileData);
        } catch (FileManagerException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.warn("Error during update of file", ex);
            throw new FileManagerException(ErrorType.UNKNOWN, "Error to update file");
        }
    }

    @Transactional(TxType.REQUIRED)
    public void delete(String fileId) {
        try {
            Optional<FileData> fileDataOptional = fileRepository.findById(fileId);
            if (fileDataOptional.isEmpty()) {
                throw new FileManagerException(ErrorType.NOT_FOUND, format("File not found with %s", fileId));
            }
            final List<String> fileNames = fileDataOptional.get()
                    .getFileVersions()
                    .stream()
                    .map(FileVersionData::getFileName)
                    .collect(Collectors.toList());


            fileNames.forEach(fileStorageService::remove);
            fileRepository.delete(fileDataOptional.get());

            LOG.info(format("Deleted file with ID=%s ", fileDataOptional.get().getId()));

        } catch (FileManagerException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.warn("Error during delete of file", ex);
            throw new FileManagerException(ErrorType.UNKNOWN, "Error to delete file");
        }

    }
}