package dev.nytt.services;

import dev.nytt.dto.FileDto;
import dev.nytt.dto.FileProcessDto;
import dev.nytt.entities.FileEntity;
import dev.nytt.exceptions.HttpCustomException;
import io.netty.util.internal.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

        if (StringUtil.isNullOrEmpty(dto.externalId())) {
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

    public void createFileByPayload(FileProcessDto fileProcessDto) {

        if (StringUtil.isNullOrEmpty(fileProcessDto.fileId()) || StringUtil.isNullOrEmpty(fileProcessDto.mimetype())) {
            throw new RuntimeException("fileId and mimetype are required");
        }
        LOG.info(String.format("process file by payload : %s", fileProcessDto.fileId()));

        if (!StringUtil.isNullOrEmpty(fileProcessDto.url())) {
            try {
                LOG.info("download of file by url");

                HttpResponse<byte[]> response = requestExternalFile(fileProcessDto.url());
                byte[] bytes = response.body();
                String fileName = generateFileName(fileProcessDto.fileId(), fileProcessDto.mimetype());
                Files.write(getUploadPath().resolve(fileName), bytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (!StringUtil.isNullOrEmpty(fileProcessDto.data())) {
            LOG.info("download file base64");
            //save file by buffer;
        }
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

    public void deleteFile(String externalId) throws HttpCustomException {
        FileEntity fileEntity = FileEntity.find("externalId", externalId).firstResult();
        if (fileEntity == null) {
            throw new HttpCustomException(404, "file not found");
        }
        try {
            FileEntity.deleteById(fileEntity.id);
        } catch (Exception e) {
            throw new HttpCustomException(500, e.getMessage());
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

    private HttpResponse<byte[]> requestExternalFile(String url) throws URISyntaxException, IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}
