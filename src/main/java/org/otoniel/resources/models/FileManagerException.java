package org.otoniel.resources.models;

public class FileManagerException extends RuntimeException {

    private ErrorType type;

    private String message;

    public FileManagerException(ErrorType type, String message) {
        this.type = type;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ErrorType getType() {
        return type;
    }

    public enum ErrorType {
        NOT_FOUND, INVALID, UNKNOWN
    }
}
