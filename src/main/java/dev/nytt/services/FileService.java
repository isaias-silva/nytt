package dev.nytt.services;

import dev.nytt.dto.FileUploadDto;
import dev.nytt.entities.FileEntity;
import dev.nytt.exceptions.HttpCustomException;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;


@ApplicationScoped
public class FileService {

    private Logger LOG;
    private final String directory = "./uploads/";

    public FileService() {
        this.LOG = Logger.getLogger(FileService.class.getName());
    }

    public FileEntity createFile(FileUploadDto dto) throws HttpCustomException {

        if (dto.fileUpload() == null) {
            throw new HttpCustomException(400, "file is required");
        }
        FileEntity fileExists=getFile(dto.externalId());
        if(fileExists!=null){
            throw new HttpCustomException(400, "file already stored");
        }
        processFile(dto.externalId(), dto.fileUpload().uploadedFile().toFile(), dto.fileUpload().contentType());

        FileEntity file = new FileEntity(dto.externalId());
        FileEntity.persist(file);
        return file;


    }

    public FileEntity getFile(String externalId) {

        return FileEntity.find("externalId", externalId).firstResult();
    }

    private void processFile(String externalId, File file, String contentType) throws HttpCustomException {
        LOG.info(String.format("process file uploaded %s", contentType));
        try {

            String[] contentInfo = contentType.split("/");
            String fileType = getFileType(contentInfo[0]);

            if (fileType == null) {
                throw new IOException("file type not supported");
            }
            byte[] buff = Files.readAllBytes(file.toPath());
            Files.write(getUploadPath().resolve(String.format("%s.%s", externalId, fileType)), buff);

        } catch (Exception e) {
            throw new HttpCustomException(400,e.getMessage());
        }
    }

    private Path getUploadPath() throws IOException {
        Path targetDirPath = Paths.get(directory);

        if (!Files.exists(targetDirPath)) {
            Files.createDirectories(targetDirPath);
        }
        return targetDirPath;
    }

    private String getFileType(String type) {
        return switch (type) {
            case "audio" -> "mp3";
            case "video" -> "mp4";
            case "image" -> "png";
            case "document" -> "pdf";
            default -> null;
        };
    }


}
