package dev.nytt.dto;

import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class FileUploadDto {

    @PartType(MediaType.TEXT_PLAIN)
    public String externalId;

    @PartType(MediaType.TEXT_PLAIN)
    public String fileName;

    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] fileData;
}