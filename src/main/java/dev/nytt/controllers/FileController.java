package dev.nytt.controllers;


import dev.nytt.dto.FileUploadDto;
import dev.nytt.exceptions.HttpCustomException;
import dev.nytt.services.FileService;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;


@Path("/file")
@Produces(MediaType.APPLICATION_JSON)


public class FileController {
    public FileController(FileService fileService) {

        this.fileService = fileService;
    }

    FileService fileService;

    @POST
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)

    public Response createFile(@RestForm("file") FileUpload file,
                               @RestForm("externalId") String externalId) throws HttpCustomException {

        FileUploadDto dto= new FileUploadDto(externalId,file);
        return Response.ok(fileService.createFile(dto)).build();
    }

    @GET
    @Transactional
    public Response getFile(@QueryParam("id") final String fileId) {
        return Response.ok(fileService.getFile(fileId)).build();
    }
}
