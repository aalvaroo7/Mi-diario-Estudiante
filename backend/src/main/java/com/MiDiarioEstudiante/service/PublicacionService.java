package com.MiDiarioEstudiante.service;

import com.MiDiarioEstudiante.http.HttpException;
import com.MiDiarioEstudiante.model.Publicacion;
import com.MiDiarioEstudiante.model.Usuario;
import com.MiDiarioEstudiante.repository.PublicacionRepository;

import java.util.List;

public class PublicacionService {
    private final PublicacionRepository publicacionRepository;
    private final UsuarioService usuarioService;

    public PublicacionService(PublicacionRepository publicacionRepository, UsuarioService usuarioService) {
        this.publicacionRepository = publicacionRepository;
        this.usuarioService = usuarioService;
    }

    public List<Publicacion> obtenerTodas() {
        return publicacionRepository.findAll();
    }

    public List<Publicacion> obtenerPorUsuario(Long usuarioId) {
        usuarioService.buscarPorId(usuarioId);
        return publicacionRepository.findByUsuarioId(usuarioId);
    }

    public Publicacion crear(Publicacion publicacion) {
        validarPublicacion(publicacion);
        Usuario autor = usuarioService.buscarPorId(publicacion.getUsuarioId());
        publicacion.setUsuarioId(autor.getId());
        return publicacionRepository.save(publicacion);
    }

    public Publicacion buscarPorId(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> HttpException.notFound("Publicación no encontrada"));
    }

    public void eliminar(Long id, Long usuarioId) {
        Publicacion publicacion = buscarPorId(id);
        if (!publicacion.getUsuarioId().equals(usuarioId)) {
            throw HttpException.forbidden("No puedes eliminar publicaciones de otros usuarios");
        }
        publicacionRepository.deleteById(id);
    }

    private void validarPublicacion(Publicacion publicacion) {
        if (publicacion.getUsuarioId() == null) {
            throw HttpException.badRequest("El usuario es obligatorio");
        }
        if (!hasText(publicacion.getContenido())) {
            throw HttpException.badRequest("El contenido no puede estar vacío");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
