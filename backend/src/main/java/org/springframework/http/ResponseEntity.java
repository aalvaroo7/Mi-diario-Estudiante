package org.springframework.http;

public class ResponseEntity<T> {
    private final T body;
    private final HttpStatus status;

    public ResponseEntity(T body, HttpStatus status) {
        this.body = body;
        this.status = status;
    }

    public static <T> BodyBuilder status(HttpStatus status) {
        return new BodyBuilder(status);
    }

    public T getBody() {
        return body;
    }

    public HttpStatus getStatusCode() {
        return status;
    }

    public static class BodyBuilder {
        private final HttpStatus status;

        private BodyBuilder(HttpStatus status) {
            this.status = status;
        }

        public <T> ResponseEntity<T> body(T body) {
            return new ResponseEntity<>(body, status);
        }
    }
}
