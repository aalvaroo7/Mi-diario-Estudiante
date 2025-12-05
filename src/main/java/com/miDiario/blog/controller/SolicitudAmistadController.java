package com.miDiario.blog.controller;

import com.miDiario.blog.model.SolicitudAmistad;
import com.miDiario.blog.service.SolicitudAmistadService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudAmistadController {

    private final SolicitudAmistadService solicitudService;

    public SolicitudAmistadController(SolicitudAmistadService solicitudService) {
        this.solicitudService = solicitudService;
    }

    // ========== ENVIAR SOLICITUD ==========
    @PostMapping("/enviar")
    public ResponseEntity<?> enviarSolicitud(
            @RequestBody Map<String, Long> body,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Long destinatarioId = body.get("destinatario_id");
        if (destinatarioId == null) {
            return ResponseEntity.badRequest().body("ID de destinatario requerido");
        }

        return solicitudService.enviarSolicitud(usuarioId, destinatarioId);
    }

    // ========== OBTENER SOLICITUDES PENDIENTES ==========
    @GetMapping("/pendientes")
    public ResponseEntity<?> obtenerSolicitudesPendientes(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        try {
            List<SolicitudAmistad> solicitudes = solicitudService.obtenerSolicitudesPendientes(usuarioId);
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== ACEPTAR SOLICITUD ==========
    @PutMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptarSolicitud(
            @PathVariable Long id,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        return solicitudService.aceptarSolicitud(id, usuarioId);
    }

    // ========== RECHAZAR SOLICITUD ==========
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarSolicitud(
            @PathVariable Long id,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        return solicitudService.rechazarSolicitud(id, usuarioId);
    }

    // ========== VERIFICAR SOLICITUD PENDIENTE ==========
    @GetMapping("/check/{destinatarioId}")
    public ResponseEntity<?> verificarSolicitudPendiente(
            @PathVariable Long destinatarioId,
            HttpSession session) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        try {
            boolean existeSolicitud = solicitudService.existeSolicitudPendiente(usuarioId, destinatarioId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("tieneSolicitud", existeSolicitud);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}