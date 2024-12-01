package dev.nytt.exceptions;

import dev.nytt.dto.ResponsePattern;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class HttpCustomExceptionMapper implements ExceptionMapper<HttpCustomException> {

    @Override
    public Response toResponse(HttpCustomException e) {
        return Response.status(e.getStatus())
                .entity(new ResponsePattern(e.getMessage(),e.getStatus())).build();
    }
}
