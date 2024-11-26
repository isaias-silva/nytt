package dev.nytt.controllers;


import dev.nytt.dto.FileUploadDto;
import dev.nytt.entities.FileEntity;
import dev.nytt.services.FileService;
import io.vertx.ext.web.FileUpload;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataOutput;

@Path("/file")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class FileController {
    public FileController( FileService fileService){
        this.fileService=fileService;
    }
    FileService fileService;
    @POST
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createFile(@MultipartForm FileUploadDto dto){

        return Response.ok(fileService.createFile(dto)).build();
    }

    @GET
    @Transactional
    public Response getFile(@QueryParam("id") final String fileId){
        return Response.ok(fileService.getFile(fileId)).build();
    }
}
