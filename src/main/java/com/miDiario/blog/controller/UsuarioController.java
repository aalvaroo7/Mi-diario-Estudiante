package com.miDiario.blog.controller;

import com.miDiario.blog.model.Usuario;
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

    // ============================================================
    // ACTUALIZAR PERFIL
    // ============================================================
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizarPerfil(
            @PathVariable Long id,
            @RequestBody Usuario usuarioDatos,
            HttpSession session) {

        Long usuarioSesionId = (Long) session.getAttribute("usuarioId");

        if (usuarioSesionId == null) {
            return ResponseEntity.status(401).body("No has iniciado sesión.");
        }

        if (!usuarioSesionId.equals(id)) {
            return ResponseEntity.status(403).body("No tienes permiso para editar este perfil.");
        }

        try {
            Usuario usuarioActualizado = usuarioService.actualizar(id, usuarioDatos);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al actualizar el perfil.");
        }
    }

    // ============================================================
    // CERRAR SESIÓN (LOGOUT) - ¡ESTO FALTABA!
    // ============================================================
    @PostMapping("/logout")
    public ResponseEntity<?> cerrarSesion(HttpSession session) {
        // Llamamos al servicio para limpiar la sesión
        usuarioService.logout(session);
        return ResponseEntity.ok("Sesión cerrada correctamente");
    }

    // ============================================================
    // BLOQUEAR USUARIO (SOLO ADMIN)
    // ============================================================
    @PutMapping("/bloquear/{id}")
    public ResponseEntity<?> bloquearUsuario(
            @PathVariable Long id,
            HttpSession session) {

        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Long adminId = (Long) session.getAttribute("usuarioId");
        return usuarioService.bloquear(adminId, id);
    }
}