package dev.nytt.dto;

public record FileProcessDto(String data,
                             String fileId,
                             String type,
                             String url,
                             String mimetype) {
}
