package org.otoniel.resources.storage;

import java.io.InputStream;

import org.otoniel.resources.models.FileMetadata;

public interface StorageService {

    /**
     * Create a file into storage
     * @param inputStream inputStream
     * @param originalFilename Optional: if not provided, service will create one.
     * @return File MetaData
     */
    public FileMetadata create(InputStream inputStream, String originalFilename);

    /**
     *
     * @param fileName Internal File name
     * @return
     */
    public byte[] read(String fileName);

    /**
     * Remove a file
     * @param fileName internal file name
     */
    void remove(String fileName);

    /**
     *  Determine if the file can be removed
     * @param fileName
     * @return
     */
    boolean canBeRemoved(String fileName);
}
