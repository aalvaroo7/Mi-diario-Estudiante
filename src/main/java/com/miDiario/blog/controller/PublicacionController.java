package com.miDiario.blog.controller;

import com.miDiario.blog.model.Publicacion;
import com.miDiario.blog.repository.PublicacionRepository;
import com.miDiario.blog.repository.UsuarioRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@RequestMapping("/api/publicaciones")
@CrossOrigin(origins = "*")
public class PublicacionController {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;

    public PublicacionController(PublicacionRepository publicacionRepository, UsuarioRepository usuarioRepository) {
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping(value = "/crear", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> crearPublicacion(
            @RequestParam("nombreUsuario") String nombreUsuario,
            @RequestParam("contenido") String contenido,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

        try {
            var usuarioOpt = usuarioRepository.findByNombreUsuario(nombreUsuario);
            if (usuarioOpt.isEmpty()) return ResponseEntity.badRequest().body("Usuario no encontrado");

            var publicacion = new Publicacion();
            publicacion.setAutor(usuarioOpt.get());
            publicacion.setContenido(contenido);
            publicacion.setFechaPublicacion(LocalDateTime.now());

            if (imagen != null && !imagen.isEmpty()) {
                // guardar imagen como url
                String prefix = "data:" + imagen.getContentType() + ";base64,";
                String base64 = Base64.getEncoder().encodeToString(imagen.getBytes());
                publicacion.setImagenUrl(prefix + base64);
            }

            publicacionRepository.save(publicacion);
            System.out.println("✅ Publicación creada: " + contenido);
            return ResponseEntity.ok("Publicación creada correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al crear publicación: " + e.getMessage());
        }
    }

    @GetMapping("/todas")
    public ResponseEntity<?> todas() {
        return ResponseEntity.ok(publicacionRepository.findAll());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        publicacionRepository.deleteById(id);
        return ResponseEntity.ok("Publicación eliminada");
    }
}
