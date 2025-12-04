package com.miDiario.blog.controller;

import com.miDiario.blog.service.PublicacionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionService publicacionService;

    public PublicacionController(PublicacionService publicacionService) {
        this.publicacionService = publicacionService;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearPublicacion(
            @RequestParam("contenido") String contenido,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo,
            HttpSession session) {

        // 1. Validamos que el usuario haya iniciado sesión
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("Sesión expirada. Por favor inicia sesión de nuevo.");
        }

        try {
            // 2. Llamamos al servicio para guardar
            return ResponseEntity.ok(publicacionService.crearPublicacion(contenido, archivo, usuarioId));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al procesar la imagen.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/todas")
    public ResponseEntity<?> obtenerTodas(HttpSession session) {
        // Validación de sesión opcional para leer, depende de tu gusto
        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        return ResponseEntity.ok(publicacionService.obtenerTodas());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        return publicacionService.eliminarPublicacion(id, usuarioId);
    }
}
