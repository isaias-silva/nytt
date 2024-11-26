package dev.nytt.services;

import dev.nytt.dto.FileUploadDto;
import dev.nytt.entities.FileEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class FileService {
    public FileEntity createFile(FileUploadDto dto){

        FileEntity file= new FileEntity(dto.externalId);
        FileEntity.persist(file);
        return file;
    }
    public FileEntity getFile(String externalId){

      return FileEntity.find("externalId", externalId).firstResult();
    }
}
