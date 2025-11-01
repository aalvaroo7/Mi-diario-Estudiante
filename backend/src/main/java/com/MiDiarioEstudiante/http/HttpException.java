package com.MiDiarioEstudiante.http;

public class HttpException extends RuntimeException {
    private final int statusCode;

    private HttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public static HttpException badRequest(String message) {
        return new HttpException(400, message);
    }

    public static HttpException unauthorized(String message) {
        return new HttpException(401, message);
    }

    public static HttpException forbidden(String message) {
        return new HttpException(403, message);
    }

    public static HttpException notFound(String message) {
        return new HttpException(404, message);
    }

    public static HttpException conflict(String message) {
        return new HttpException(409, message);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
