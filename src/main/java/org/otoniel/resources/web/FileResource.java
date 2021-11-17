package org.otoniel.resources.web;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.hibernate.internal.util.StringHelper.isEmpty;
import static org.otoniel.resources.models.FileManagerException.ErrorType.INVALID;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.graalvm.collections.Pair;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.otoniel.resources.manager.FileManager;
import org.otoniel.resources.models.FileManagerException;
import org.otoniel.resources.models.FileManagerException.ErrorType;
import org.otoniel.resources.models.FileObject;
import org.otoniel.resources.models.FileRequest;

@Path("/files/")
@Produces(MediaType.APPLICATION_JSON)
public class FileResource {

    @Inject
    FileManager fileManager;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@MultipartForm FileRequest upload) {
        if (isNull(upload.getFile()) || isEmpty(upload.getFileName())) {
            throw new FileManagerException(INVALID, "File was not provided");
        }
        final FileObject fileObject = fileManager.create(upload.getFile(), upload.getFileName());
        return Response.ok(fileObject).build();
    }

    @GET
    @Path("/{file_id}")
    public Response getFileInfo(@PathParam("file_id") String id) {
        FileObject fileObject = fileManager.get(id);
        return Response.ok(fileObject).build();
    }

    @PUT
    @Path("/{file_id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response update(@PathParam("file_id") String fileId, @MultipartForm FileRequest upload) {
        if (isEmpty(fileId) && isNull(upload.getFile()) || isEmpty(upload.getFileName())) {
            throw new FileManagerException(INVALID, "File was not provided");
        }

        return Response.ok(fileManager.update(fileId, upload.getFile(), upload.getFileName())).build();
    }


    @GET
    @Path("/{file_id}/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@PathParam("file_id") String id, @QueryParam("version") String version) {
        if (isEmpty(id)) {
            throw new FileManagerException(INVALID, "file_id must be provided");
        }

        Pair<String, byte[]> content = fileManager.readFile(id, version);
        if (isNull(content)) {
            throw new FileManagerException(ErrorType.NOT_FOUND, "File was not found.");
        }

        return Response.ok(content.getRight())
                .header("Content-Disposition", "attachment; filename=\"" + content.getLeft() + "\"")
                .build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFiles(@QueryParam("page_size") Integer pageSize, @QueryParam("page_number") Integer pageNumber) {
        Integer pageSizeParam = ofNullable(pageSize).orElse(100);
        Integer pageNumberParam = ofNullable(pageNumber).orElse(0);

        final List<FileObject> list = fileManager.list(pageNumberParam, pageSizeParam);

        return Response.ok(list).build();
    }

    @DELETE
    @Path("/{file_id}")
    public Response delete(@PathParam("file_id") String fileId) {
        if (isEmpty(fileId)) {
            throw new FileManagerException(INVALID, "file_id must be provided");
        }

        fileManager.delete(fileId);

        return Response.status(204).build();
    }




}