package com.miDiario.blog.service;

import com.miDiario.blog.model.SolicitudAmistad;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.repository.SolicitudAmistadRepository;
import com.miDiario.blog.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SolicitudAmistadService {

    private final SolicitudAmistadRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final AmistadService amistadService;

    public SolicitudAmistadService(SolicitudAmistadRepository solicitudRepository,
                                   UsuarioRepository usuarioRepository,
                                   AmistadService amistadService) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.amistadService = amistadService;
    }

    // Enviar solicitud de amistad
    @Transactional
    public ResponseEntity<?> enviarSolicitud(Long solicitanteId, Long destinatarioId) {
        try {
            // Validar que no sean el mismo usuario
            if (solicitanteId.equals(destinatarioId)) {
                return ResponseEntity.badRequest().body("No puedes enviarte una solicitud a ti mismo");
            }

            // Obtener usuarios
            Usuario solicitante = usuarioRepository.findById(solicitanteId)
                    .orElseThrow(() -> new RuntimeException("Usuario solicitante no encontrado"));
            Usuario destinatario = usuarioRepository.findById(destinatarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario destinatario no encontrado"));

            // Verificar si ya son amigos
            if (amistadService.sonAmigos(solicitanteId, destinatarioId)) {
                return ResponseEntity.badRequest().body("Ya son amigos");
            }

            // Verificar si ya existe solicitud pendiente
            if (solicitudRepository.existsSolicitudPendiente(solicitante, destinatario)) {
                return ResponseEntity.badRequest().body("Ya existe una solicitud pendiente");
            }

            // Crear nueva solicitud
            SolicitudAmistad solicitud = new SolicitudAmistad();
            solicitud.setSolicitante(solicitante);
            solicitud.setDestinatario(destinatario);
            solicitud.setStatus(SolicitudAmistad.EstadoSolicitud.PENDIENTE);

            solicitudRepository.save(solicitud);

            return ResponseEntity.ok("Solicitud enviada correctamente");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al enviar solicitud");
        }
    }

    // Obtener solicitudes pendientes de un usuario
    public List<SolicitudAmistad> obtenerSolicitudesPendientes(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return solicitudRepository.findByDestinatarioAndStatus(
                usuario, SolicitudAmistad.EstadoSolicitud.PENDIENTE);
    }

    // Aceptar solicitud
    @Transactional
    public ResponseEntity<?> aceptarSolicitud(Long solicitudId, Long usuarioId) {
        try {
            SolicitudAmistad solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            // Validar que el usuario es el destinatario
            if (!solicitud.getDestinatario().getId().equals(usuarioId)) {
                return ResponseEntity.status(403).body("No tienes permiso para aceptar esta solicitud");
            }

            // Cambiar estado de la solicitud
            solicitud.setStatus(SolicitudAmistad.EstadoSolicitud.ACEPTADA);
            solicitudRepository.save(solicitud);

            // Crear amistad
            return amistadService.crearAmistad(
                    solicitud.getSolicitante().getId(),
                    solicitud.getDestinatario().getId()
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al aceptar solicitud");
        }
    }

    // Rechazar solicitud
    @Transactional
    public ResponseEntity<?> rechazarSolicitud(Long solicitudId, Long usuarioId) {
        try {
            SolicitudAmistad solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            // Validar que el usuario es el destinatario
            if (!solicitud.getDestinatario().getId().equals(usuarioId)) {
                return ResponseEntity.status(403).body("No tienes permiso para rechazar esta solicitud");
            }

            // Cambiar estado de la solicitud
            solicitud.setStatus(SolicitudAmistad.EstadoSolicitud.RECHAZADA);
            solicitudRepository.save(solicitud);

            return ResponseEntity.ok("Solicitud rechazada");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al rechazar solicitud");
        }
    }

    // Verificar si hay solicitud pendiente entre dos usuarios
    public boolean existeSolicitudPendiente(Long usuario1Id, Long usuario2Id) {
        Usuario usuario1 = usuarioRepository.findById(usuario1Id)
                .orElseThrow(() -> new RuntimeException("Usuario 1 no encontrado"));
        Usuario usuario2 = usuarioRepository.findById(usuario2Id)
                .orElseThrow(() -> new RuntimeException("Usuario 2 no encontrado"));

        return solicitudRepository.existsSolicitudPendiente(usuario1, usuario2);
    }
}