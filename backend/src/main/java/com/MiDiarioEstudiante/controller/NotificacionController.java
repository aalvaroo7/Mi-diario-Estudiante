package com.MiDiarioEstudiante.controller;

import com.MiDiarioEstudiante.model.Notificacion;
import com.MiDiarioEstudiante.model.enums.TipoNotificacion;
import com.MiDiarioEstudiante.service.NotificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Notificacion> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return notificacionService.obtenerPorUsuario(usuarioId);
    }

    @PostMapping
    public ResponseEntity<Notificacion> crear(@RequestParam Long usuarioId,
                                              @RequestParam String mensaje,
                                              @RequestParam(required = false) TipoNotificacion tipo) {
        Notificacion notificacion = notificacionService.crearNotificacion(usuarioId, mensaje, tipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacion);
    }

    @PatchMapping("/{id}/leida")
    public Notificacion marcarComoLeida(@PathVariable Long id) {
        return notificacionService.marcarComoLeida(id);
    }
}
