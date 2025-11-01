package com.MiDiarioEstudiante.service;

import com.MiDiarioEstudiante.http.HttpException;
import com.MiDiarioEstudiante.model.Comentario;
import com.MiDiarioEstudiante.model.Publicacion;
import com.MiDiarioEstudiante.model.Usuario;
import com.MiDiarioEstudiante.model.enums.TipoNotificacion;
import com.MiDiarioEstudiante.repository.ComentarioRepository;

import java.util.List;

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
        return comentarioRepository.findByPublicacionId(publicacionId);
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
                .orElseThrow(() -> HttpException.notFound("Comentario no encontrado"));
        if (!comentario.getUsuarioId().equals(usuarioId)) {
            throw HttpException.forbidden("Solo puedes eliminar tus comentarios");
        }
        comentarioRepository.deleteById(comentarioId);
    }

    private void validarComentario(Comentario comentario) {
        if (comentario.getPublicacionId() == null) {
            throw HttpException.badRequest("La publicación es obligatoria");
        }
        if (comentario.getUsuarioId() == null) {
            throw HttpException.badRequest("El usuario es obligatorio");
        }
        if (!hasText(comentario.getContenido())) {
            throw HttpException.badRequest("El contenido no puede estar vacío");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
