package com.MiDiarioEstudiante.service;

import com.MiDiarioEstudiante.http.HttpException;
import com.MiDiarioEstudiante.model.Notificacion;
import com.MiDiarioEstudiante.model.Usuario;
import com.MiDiarioEstudiante.model.enums.TipoNotificacion;
import com.MiDiarioEstudiante.repository.NotificacionRepository;

import java.util.List;

public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final UsuarioService usuarioService;

    public NotificacionService(NotificacionRepository notificacionRepository, UsuarioService usuarioService) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioService = usuarioService;
    }

    public Notificacion crearNotificacion(Long usuarioId, String mensaje, TipoNotificacion tipo) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        if (!hasText(mensaje)) {
            throw HttpException.badRequest("El mensaje de la notificación es obligatorio");
        }
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioId(usuario.getId());
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo != null ? tipo : TipoNotificacion.GENERAL);
        notificacion.setLeido(false);
        return notificacionRepository.save(notificacion);
    }

    public List<Notificacion> obtenerPorUsuario(Long usuarioId) {
        usuarioService.buscarPorId(usuarioId);
        return notificacionRepository.findByUsuarioId(usuarioId);
    }

    public Notificacion marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> HttpException.notFound("Notificación no encontrada"));
        notificacion.setLeido(true);
        return notificacionRepository.save(notificacion);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
