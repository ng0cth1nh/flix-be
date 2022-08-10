package com.fu.flix.dto.error;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

public class GeneralException extends ResponseStatusException {
    private final List<String> messageParams;
    public GeneralException(HttpStatus status, String reason, String... params) {
        super(status, reason);
        this.messageParams = params == null ? null : Arrays.asList(params);
    }

    @Override
    public String getMessage() {
        return super.getReason();
    }

    public List<String> getMessageParams() {
        return messageParams;
    }
}
