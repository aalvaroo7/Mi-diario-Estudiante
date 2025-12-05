package com.miDiario.blog.controller;

import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    // ========== BUSCAR USUARIOS ==========
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarUsuarios(
            @RequestParam String q,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        try {
            // Buscar usuarios por nombre, apellido o nombre de usuario
            List<Usuario> usuarios = usuarioService.buscarUsuarios(q, usuarioId);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== OBTENER USUARIO POR ID ==========
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(
            @PathVariable Long id,
            HttpSession session) {

        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        try {
            Usuario usuario = usuarioService.obtenerPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
