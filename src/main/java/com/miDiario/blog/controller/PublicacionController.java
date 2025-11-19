package com.miDiario.blog.controller;

import com.miDiario.blog.model.Publicacion;
import com.miDiario.blog.service.PublicacionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionService publicacionService;

    public PublicacionController(PublicacionService publicacionService) {
        this.publicacionService = publicacionService;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearPublicacion(
            @RequestBody Publicacion publicacion,
            HttpSession session) {

        // 🔐 VALIDACIÓN DE SESIÓN
        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        return ResponseEntity.ok(publicacionService.crearPublicacion(publicacion, usuarioId));
    }

    @GetMapping("/todas")
    public ResponseEntity<?> obtenerTodas(HttpSession session) {

        // 🔐 VALIDACIÓN DE SESIÓN
        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        return ResponseEntity.ok(publicacionService.obtenerTodas());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, HttpSession session) {

        // 🔐 VALIDACIÓN DE SESIÓN
        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        return publicacionService.eliminarPublicacion(id, usuarioId);
    }
}
