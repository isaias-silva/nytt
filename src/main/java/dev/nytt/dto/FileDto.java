package dev.nytt.dto;


import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;

public record FileDto(String externalId,
                      File file, String mimetype) { }
