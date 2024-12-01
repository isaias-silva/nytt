package dev.nytt.exceptions;

public class HttpCustomException extends Throwable {
    private final int status;
    private final String message;

    public HttpCustomException(int status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
}
