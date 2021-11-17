package org.otoniel.resources.web;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.otoniel.resources.models.FileManagerException;
import org.otoniel.resources.models.FileManagerException.ErrorType;

@Provider
public class FileManagerExceptionHandler implements ExceptionMapper<FileManagerException> {

    @Override
    public Response toResponse(FileManagerException e) {
        return Response.status(mapErrorCode(e.getType())).
                entity(new FileManagerGenericResponse(e.getMessage()))
                .build();
    }

    private Status mapErrorCode(ErrorType type) {
        if (ErrorType.NOT_FOUND.equals(type)) {
            return NOT_FOUND;
        } else if (ErrorType.INVALID.equals(type)) {
            return BAD_REQUEST;
        }
        return INTERNAL_SERVER_ERROR;
    }


}