package org.springframework.http;

public enum HttpStatus {
    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409);

    private final int value;

    HttpStatus(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
