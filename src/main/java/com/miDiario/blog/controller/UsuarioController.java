package com.miDiario.blog.controller;

import com.miDiario.blog.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // SOLO gestión de usuarios (p.ej. bloqueo por admin)
    @PutMapping("/bloquear/{id}")
    public ResponseEntity<?> bloquearUsuario(
            @PathVariable Long id,
            HttpSession session) {

        // Validación de sesión
        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Long adminId = (Long) session.getAttribute("usuarioId");
        return usuarioService.bloquear(adminId, id);
    }
}
