package com.MiDiarioEstudiante.backend.service;

import com.MiDiarioEstudiante.backend.model.Notificacion;
import com.MiDiarioEstudiante.backend.model.Usuario;
import com.MiDiarioEstudiante.backend.model.enums.TipoNotificacion;
import com.MiDiarioEstudiante.backend.repository.NotificacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final UsuarioService usuarioService;

    public NotificacionService(NotificacionRepository notificacionRepository, UsuarioService usuarioService) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioService = usuarioService;
    }

    public Notificacion crearNotificacion(Long usuarioId, String mensaje, TipoNotificacion tipo) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        if (!StringUtils.hasText(mensaje)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje de la notificación es obligatorio");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada"));
        notificacion.setLeido(true);
        return notificacionRepository.save(notificacion);
    }
}
