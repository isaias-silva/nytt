package dev.nytt.services;

import dev.nytt.dto.FileUploadDto;
import dev.nytt.entities.FileEntity;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


@ApplicationScoped
public class FileService {
    public FileEntity createFile(FileUploadDto dto) throws IOException {


            byte[] data = extractFileBytes(dto.fileUpload().uploadedFile().toFile());

            FileEntity file = new FileEntity(dto.externalId());
            FileEntity.persist(file);
            return file;


    }

    public FileEntity getFile(String externalId) {

        return FileEntity.find("externalId", externalId).firstResult();
    }

    private byte[] extractFileBytes(File file) throws IOException {

        return Files.readAllBytes(file.toPath());

    }


}
