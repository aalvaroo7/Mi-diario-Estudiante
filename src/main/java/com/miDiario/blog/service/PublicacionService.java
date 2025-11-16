package com.miDiario.blog.service;

import com.miDiario.blog.model.Publicacion;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.repository.PublicacionRepository;
import com.miDiario.blog.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;

    public PublicacionService(PublicacionRepository publicacionRepository,
                              UsuarioRepository usuarioRepository) {
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ============================================================
    // CREAR PUBLICACIÓN (método que espera tu controlador)
    // ============================================================
    public Publicacion crearPublicacion(Publicacion publicacion, Long usuarioId) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return null; // o lanzar excepción si prefieres
        }

        Usuario usuario = usuarioOpt.get();
        publicacion.setUsuario(usuario);

        return publicacionRepository.save(publicacion);
    }

    // ============================================================
    // OBTENER TODAS (método que espera tu controlador)
    // ============================================================
    public List<Publicacion> obtenerTodas() {
        return publicacionRepository.findAll();
    }

    // ============================================================
    // ELIMINAR (método que espera tu controlador)
    // ============================================================
    public ResponseEntity<?> eliminarPublicacion(Long idPublicacion, Long idUsuario) {

        Optional<Publicacion> pubOpt = publicacionRepository.findById(idPublicacion);
        if (pubOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Publicación no encontrada");
        }

        Publicacion p = pubOpt.get();

        // Validar que la publicación pertenece al usuario
        if (!p.getUsuario().getId().equals(idUsuario)) {
            return ResponseEntity.status(403).body("No puedes eliminar esta publicación");
        }

        publicacionRepository.delete(p);

        return ResponseEntity.ok("Publicación eliminada correctamente");
    }
}
