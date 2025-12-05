package com.miDiario.blog.service;

import com.miDiario.blog.model.Amistad;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.repository.AmistadRepository;
import com.miDiario.blog.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmistadService {

    private final AmistadRepository amistadRepository;
    private final UsuarioRepository usuarioRepository;

    public AmistadService(AmistadRepository amistadRepository,
                          UsuarioRepository usuarioRepository) {
        this.amistadRepository = amistadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Crear amistad
    @Transactional
    public ResponseEntity<?> crearAmistad(Long usuario1Id, Long usuario2Id) {
        try {
            // Validar que no sean el mismo usuario
            if (usuario1Id.equals(usuario2Id)) {
                return ResponseEntity.badRequest().body("No puedes ser amigo de ti mismo");
            }

            // Obtener usuarios
            Usuario usuario1 = usuarioRepository.findById(usuario1Id)
                    .orElseThrow(() -> new RuntimeException("Usuario 1 no encontrado"));
            Usuario usuario2 = usuarioRepository.findById(usuario2Id)
                    .orElseThrow(() -> new RuntimeException("Usuario 2 no encontrado"));

            // Verificar si ya son amigos
            if (amistadRepository.sonAmigos(usuario1, usuario2)) {
                return ResponseEntity.badRequest().body("Ya son amigos");
            }

            // Crear amistad
            Amistad amistad = new Amistad();
            amistad.setUsuario1(usuario1);
            amistad.setUsuario2(usuario2);

            amistadRepository.save(amistad);

            return ResponseEntity.ok("Amistad creada correctamente");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al crear amistad");
        }
    }

    // Obtener amigos de un usuario
    public List<Usuario> obtenerAmigos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Amistad> amistades = amistadRepository.findByUsuario(usuario);

        return amistades.stream()
                .map(amistad -> {
                    if (amistad.getUsuario1().getId().equals(usuarioId)) {
                        return amistad.getUsuario2();
                    } else {
                        return amistad.getUsuario1();
                    }
                })
                .collect(Collectors.toList());
    }

    // Eliminar amistad
    @Transactional
    public ResponseEntity<?> eliminarAmistad(Long amistadId, Long usuarioId) {
        try {
            Amistad amistad = amistadRepository.findById(amistadId)
                    .orElseThrow(() -> new RuntimeException("Amistad no encontrada"));

            // Validar que el usuario pertenece a la amistad
            if (!amistad.getUsuario1().getId().equals(usuarioId) &&
                    !amistad.getUsuario2().getId().equals(usuarioId)) {
                return ResponseEntity.status(403).body("No tienes permiso para eliminar esta amistad");
            }

            amistadRepository.delete(amistad);

            return ResponseEntity.ok("Amistad eliminada");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al eliminar amistad");
        }
    }

    // Verificar si dos usuarios son amigos
    public boolean sonAmigos(Long usuario1Id, Long usuario2Id) {
        Usuario usuario1 = usuarioRepository.findById(usuario1Id)
                .orElseThrow(() -> new RuntimeException("Usuario 1 no encontrado"));
        Usuario usuario2 = usuarioRepository.findById(usuario2Id)
                .orElseThrow(() -> new RuntimeException("Usuario 2 no encontrado"));

        return amistadRepository.sonAmigos(usuario1, usuario2);
    }

    // Buscar amistad por ID de usuario
    public Amistad buscarAmistadPorUsuarios(Long usuario1Id, Long usuario2Id) {
        Usuario usuario1 = usuarioRepository.findById(usuario1Id)
                .orElseThrow(() -> new RuntimeException("Usuario 1 no encontrado"));
        Usuario usuario2 = usuarioRepository.findById(usuario2Id)
                .orElseThrow(() -> new RuntimeException("Usuario 2 no encontrado"));

        return amistadRepository.findByUsuarios(usuario1, usuario2)
                .orElseThrow(() -> new RuntimeException("Amistad no encontrada"));
    }
}