package com.fu.flix.dto.error;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GeneralException extends ResponseStatusException {
    public GeneralException(HttpStatus status, String reason) {
        super(status, reason);
    }

    @Override
    public String getMessage() {
        return super.getReason();
    }
}
