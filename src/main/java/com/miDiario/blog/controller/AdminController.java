package com.miDiario.blog.controller;

import com.miDiario.blog.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ============================================================
    // BLOQUEAR USUARIO
    // ============================================================
    @PostMapping("/bloquear/{usuarioId}")
    public ResponseEntity<?> bloquearUsuario(@PathVariable Long usuarioId,
                                             HttpSession session) {

        Long adminId = (Long) session.getAttribute("usuarioId");

        if (adminId == null)
            return ResponseEntity.status(401).body("Debes iniciar sesión");

        String rol = (String) session.getAttribute("rol");

        if (!"ADMIN".equalsIgnoreCase(rol))
            return ResponseEntity.status(403).body("No tienes permisos");

        return usuarioService.bloquear(adminId, usuarioId);
    }

    // ============================================================
    // DESBLOQUEAR USUARIO
    // ============================================================
    @PostMapping("/desbloquear/{usuarioId}")
    public ResponseEntity<?> desbloquearUsuario(@PathVariable Long usuarioId,
                                                HttpSession session) {

        Long adminId = (Long) session.getAttribute("usuarioId");

        if (adminId == null)
            return ResponseEntity.status(401).body("Debes iniciar sesión");

        String rol = (String) session.getAttribute("rol");

        if (!"ADMIN".equalsIgnoreCase(rol))
            return ResponseEntity.status(403).body("No tienes permisos");

        return usuarioService.desbloquear(adminId, usuarioId);
    }
}

