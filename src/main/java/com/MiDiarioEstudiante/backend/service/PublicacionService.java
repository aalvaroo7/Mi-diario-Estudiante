package com.MiDiarioEstudiante.backend.service;

import com.MiDiarioEstudiante.backend.model.Publicacion;
import com.MiDiarioEstudiante.backend.model.Usuario;
import com.MiDiarioEstudiante.backend.repository.PublicacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));
    }

    public void eliminar(Long id, Long usuarioId) {
        Publicacion publicacion = buscarPorId(id);
        if (!publicacion.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar publicaciones de otros usuarios");
        }
        publicacionRepository.deleteById(id);
    }

    private void validarPublicacion(Publicacion publicacion) {
        if (publicacion.getUsuarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario es obligatorio");
        }
        if (!StringUtils.hasText(publicacion.getContenido())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El contenido no puede estar vacío");
        }
    }
}
