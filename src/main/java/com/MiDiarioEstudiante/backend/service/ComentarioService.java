package com.MiDiarioEstudiante.backend.service;

import com.MiDiarioEstudiante.backend.model.Comentario;
import com.MiDiarioEstudiante.backend.model.Publicacion;
import com.MiDiarioEstudiante.backend.model.Usuario;
import com.MiDiarioEstudiante.backend.model.enums.TipoNotificacion;
import com.MiDiarioEstudiante.backend.repository.ComentarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ComentarioService {
    private final ComentarioRepository comentarioRepository;
    private final PublicacionService publicacionService;
    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             PublicacionService publicacionService,
                             UsuarioService usuarioService,
                             NotificacionService notificacionService) {
        this.comentarioRepository = comentarioRepository;
        this.publicacionService = publicacionService;
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
    }

    public List<Comentario> obtenerPorPublicacion(Long publicacionId) {
        publicacionService.buscarPorId(publicacionId);
        return comentarioRepository.findByPublicacionIdOrderByFechaCreacionAsc(publicacionId);
    }

    public Comentario crear(Comentario comentario) {
        validarComentario(comentario);
        Publicacion publicacion = publicacionService.buscarPorId(comentario.getPublicacionId());
        Usuario autor = usuarioService.buscarPorId(comentario.getUsuarioId());
        comentario.setUsuarioId(autor.getId());
        Comentario guardado = comentarioRepository.save(comentario);
        if (!publicacion.getUsuarioId().equals(autor.getId())) {
            String mensaje = autor.getNombre() + " comentó tu publicación";
            notificacionService.crearNotificacion(publicacion.getUsuarioId(), mensaje, TipoNotificacion.COMENTARIO);
        }
        return guardado;
    }

    public void eliminar(Long comentarioId, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));
        if (!comentario.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes eliminar tus comentarios");
        }
        comentarioRepository.deleteById(comentarioId);
    }

    private void validarComentario(Comentario comentario) {
        if (comentario.getPublicacionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicación es obligatoria");
        }
        if (comentario.getUsuarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario es obligatorio");
        }
        if (!StringUtils.hasText(comentario.getContenido())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El contenido no puede estar vacío");
        }
    }
}
