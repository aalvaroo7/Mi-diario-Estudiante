package com.miDiario.blog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> manejarErroresDeNegocio(RuntimeException ex) {
        return ResponseEntity.badRequest().body(respuesta("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> manejarErroresDeSeguridad(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarErroresDeValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Solicitud inválida");
        return ResponseEntity.badRequest().body(respuesta("mensaje", mensaje));
    }

    private Map<String, String> respuesta(String clave, String valor) {
        Map<String, String> mapa = new HashMap<>();
        mapa.put(clave, valor);
        return mapa;
    }
}
