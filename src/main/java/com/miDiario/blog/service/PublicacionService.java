package com.miDiario.blog.service;

import com.miDiario.blog.model.Publicacion;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.repository.PublicacionRepository;
import com.miDiario.blog.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
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

    public Publicacion crearPublicacion(String contenido, MultipartFile archivo, Long usuarioId) throws IOException {
        // 1. Buscamos al usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // 2. Preparamos la publicación
        Publicacion nuevaPublicacion = new Publicacion();
        nuevaPublicacion.setContenido(contenido);
        nuevaPublicacion.setUsuario(usuarioOpt.get());
        nuevaPublicacion.setFechaPublicacion(LocalDateTime.now());

        // 3. Procesamos la imagen si existe
        if (archivo != null && !archivo.isEmpty()) {
            // Convertimos los bytes de la imagen a String Base64
            byte[] bytes = archivo.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(bytes);

            // Guardamos con el formato correcto para que HTML lo entienda (data:image/png;base64,...)
            nuevaPublicacion.setImagenUrl("data:" + archivo.getContentType() + ";base64," + base64Image);
        }

        // 4. Guardamos en Base de Datos
        return publicacionRepository.save(nuevaPublicacion);
    }

    public List<Publicacion> obtenerTodas() {
        // Devuelve las más recientes primero (Opcional: añadir un OrderBy en el repositorio)
        return publicacionRepository.findAll();
    }

    public ResponseEntity<?> eliminarPublicacion(Long idPublicacion, Long idUsuario) {
        Optional<Publicacion> pubOpt = publicacionRepository.findById(idPublicacion);
        if (pubOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Publicación no encontrada");
        }

        Publicacion p = pubOpt.get();

        // Solo el dueño puede borrarla
        if (!p.getUsuario().getId().equals(idUsuario)) {
            return ResponseEntity.status(403).body("No tienes permiso para eliminar esto");
        }

        publicacionRepository.delete(p);
        return ResponseEntity.ok("Eliminada");
    }
}