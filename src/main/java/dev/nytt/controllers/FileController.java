package dev.nytt.controllers;


import dev.nytt.dto.FileDto;
import dev.nytt.exceptions.HttpCustomException;
import dev.nytt.services.FileService;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


@Path("/file")
@Produces(MediaType.APPLICATION_JSON)


public class FileController {
    public FileController(FileService fileService) {

        this.fileService = fileService;
    }

    FileService fileService;

    @POST
    @Operation(summary = "upload de arquivo")
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)

    public Response createFile(@RestForm("file") FileUpload file,
                               @RestForm("externalId") String externalId) throws HttpCustomException {

        if (file == null) {
            throw new HttpCustomException(400, "file is required");
        }
        FileDto dto = new FileDto(externalId, file.uploadedFile().toFile(), file.contentType());

        return Response.ok(fileService.createFile(dto)).build();
    }

    @GET
    @Operation(summary = "retorna arquivo via external Id")
    @Transactional
    public Response getFile(@QueryParam("id") final String fileId) throws HttpCustomException, IOException {
        File file = fileService.getFile(fileId);
        String mimeType = Files.probeContentType(file.toPath());
        return Response.ok(file).type(mimeType).build();
    }
}
