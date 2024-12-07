package dev.nytt.services;

import dev.nytt.dto.FileDto;
import dev.nytt.entities.FileEntity;
import dev.nytt.exceptions.HttpCustomException;
import io.netty.util.internal.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Logger;


@ApplicationScoped
public class FileService {

    private final Logger LOG;
    private static final String directory = "./uploads/";

    public FileService() {
        this.LOG = Logger.getLogger(FileService.class.getName());
    }

    public FileEntity createFile(FileDto dto) throws HttpCustomException {

        if(StringUtil.isNullOrEmpty(dto.externalId())){
            throw new HttpCustomException(400, "external id is required");

        }
        FileEntity fileExists = getFileEntity(dto.externalId());
        if (fileExists != null) {
            throw new HttpCustomException(400, "file already stored");
        }

        String fileName = generateFileName(dto.externalId(), dto.mimetype());
        processFile(fileName, dto.file());

        FileEntity fileEntity = new FileEntity(dto.externalId(), fileName);
        FileEntity.persist(fileEntity);

        return fileEntity;


    }

    public FileEntity getFileEntity(String externalId) {

        return FileEntity.find("externalId", externalId).firstResult();
    }

    public File getFile(String externalId) throws HttpCustomException {
        FileEntity fileEntity = FileEntity.find("externalId", externalId).firstResult();
        if (fileEntity == null) {
            throw new HttpCustomException(404, "file not found");
        }
        try {
            Path uploadPath = getUploadPath();
            File file = new File(uploadPath.toString(), fileEntity.fileName);

            if (!file.exists()) {
                throw new HttpCustomException(404, "file not found");
            }
            return file;

        } catch (IOException e) {
            throw new HttpCustomException(500, e.getMessage());
        } catch (HttpCustomException e) {
            throw new HttpCustomException(e.getStatus(), e.getMessage());
        }
    }

    private void processFile(String fileName, File file) throws HttpCustomException {

        try {
            LOG.info(String.format("process file --- %s", fileName));
            byte[] buff = Files.readAllBytes(file.toPath());
            Files.write(getUploadPath().resolve(fileName), buff);

        } catch (Exception e) {
            throw new HttpCustomException(400, e.getMessage());
        }
    }

    private Path getUploadPath() throws IOException {
        Path targetDirPath = Paths.get(directory);

        if (!Files.exists(targetDirPath)) {
            Files.createDirectories(targetDirPath);
        }
        return targetDirPath;
    }

    private String generateFileName(String externalId, String mimeType) {

        String type = mimeType.split("/")[1];
        String name = (externalId + UUID.randomUUID()).trim().replace(" ", "-");

        return String.format("%s.%s", name, type);

    }

}
