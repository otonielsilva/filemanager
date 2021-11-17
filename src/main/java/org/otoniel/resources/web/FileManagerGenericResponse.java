package org.otoniel.resources.web;

public class FileManagerGenericResponse {

    private String errorMessage;

    public FileManagerGenericResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}