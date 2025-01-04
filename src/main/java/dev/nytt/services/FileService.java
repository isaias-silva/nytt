package dev.nytt.services;

import dev.nytt.dto.FileDto;
import dev.nytt.dto.FileProcessDto;
import dev.nytt.entities.FileEntity;
import dev.nytt.exceptions.HttpCustomException;
import io.netty.util.internal.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.io.File;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
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

    @Transactional
    public void createFileByPayload(FileProcessDto fileProcessDto) {

        if (StringUtil.isNullOrEmpty(fileProcessDto.fileId()) || StringUtil.isNullOrEmpty(fileProcessDto.mimetype())) {
            throw new RuntimeException("fileId and mimetype are required");
        }
        FileEntity fileDb = FileEntity.find("externalId", fileProcessDto.fileId()).firstResult();
        if (fileDb != null) {
            throw new RuntimeException("external id already in use");
        }
        LOG.info(String.format("process file by payload: %s", fileProcessDto.fileId()));


        try {
            byte[] bytes = new byte[0];
            if (!StringUtil.isNullOrEmpty(fileProcessDto.url())) {
                LOG.info("download file by link");
                HttpResponse<byte[]> response = requestExternalFile(fileProcessDto.url());
                if (response.statusCode() != 200) {
                    throw new RuntimeException(String.format("error in request %s", response.statusCode()));
                }
                bytes = response.body();

            } else if (!StringUtil.isNullOrEmpty(fileProcessDto.data())) {
                LOG.info("convert file by base64");
                bytes = Base64.getDecoder().decode(fileProcessDto.data());

            }

            String fileName = generateFileName(fileProcessDto.fileId(), fileProcessDto.mimetype());

            Files.write(getUploadPath().resolve(fileName), bytes);

            FileEntity fileRegister = new FileEntity(fileProcessDto.fileId(), fileName);
            FileEntity.persist(fileRegister);

        } catch (Exception e) {
            throw new RuntimeException(e);
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
