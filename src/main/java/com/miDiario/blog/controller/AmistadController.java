package com.miDiario.blog.controller;

import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.service.AmistadService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/amigos")
public class AmistadController {

    private final AmistadService amistadService;

    public AmistadController(AmistadService amistadService) {
        this.amistadService = amistadService;
    }

    // ========== OBTENER AMIGOS ==========
    @GetMapping("/lista")
    public ResponseEntity<?> obtenerAmigos(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        try {
            List<Usuario> amigos = amistadService.obtenerAmigos(usuarioId);
            return ResponseEntity.ok(amigos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== ELIMINAR AMIGO ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarAmigo(
            @PathVariable Long id,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        return amistadService.eliminarAmistad(id, usuarioId);
    }

    // ========== VERIFICAR SI SON AMIGOS ==========
    @GetMapping("/check/{otroUsuarioId}")
    public ResponseEntity<?> verificarAmistad(
            @PathVariable Long otroUsuarioId,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        try {
            boolean sonAmigos = amistadService.sonAmigos(usuarioId, otroUsuarioId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("esAmigo", sonAmigos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}