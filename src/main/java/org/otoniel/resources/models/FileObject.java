package org.otoniel.resources.models;

import java.time.LocalDateTime;
import java.util.Objects;


public class FileObject {

    public String id;
    public String originalFileName;
    public int lastVersion;
    private long size;
    private String checksum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(int lastVersion) {
        this.lastVersion = lastVersion;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileObject that = (FileObject) o;
        return lastVersion == that.lastVersion && size == that.size && Objects.equals(id, that.id)
                && Objects.equals(originalFileName, that.originalFileName)
                && Objects.equals(checksum, that.checksum) && Objects.equals(createdAt,
                that.createdAt) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public String toString() {
        return "FileObject{" +
                "id='" + id + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", lastVersion=" + lastVersion +
                ", checksum='" + checksum + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
