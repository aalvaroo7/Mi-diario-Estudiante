package org.springframework.web.server;

import org.springframework.http.HttpStatus;

public class ResponseStatusException extends RuntimeException {
    private final HttpStatus status;

    public ResponseStatusException(HttpStatus status, String reason) {
        super(reason);
        this.status = status;
    }

    public HttpStatus getStatusCode() {
        return status;
    }
}
