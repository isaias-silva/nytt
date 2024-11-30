package dev.nytt.dto;


import org.jboss.resteasy.reactive.multipart.FileUpload;

public record FileUploadDto(String externalId, FileUpload fileUpload) { }
