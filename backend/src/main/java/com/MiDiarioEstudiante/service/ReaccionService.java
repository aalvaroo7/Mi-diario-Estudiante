package com.MiDiarioEstudiante.service;

import com.MiDiarioEstudiante.model.Publicacion;
import com.MiDiarioEstudiante.model.Reaccion;
import com.MiDiarioEstudiante.model.Usuario;
import com.MiDiarioEstudiante.model.enums.TipoNotificacion;
import com.MiDiarioEstudiante.model.enums.TipoReaccion;
import com.MiDiarioEstudiante.repository.ReaccionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReaccionService {
    private final ReaccionRepository reaccionRepository;
    private final PublicacionService publicacionService;
    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;

    public ReaccionService(ReaccionRepository reaccionRepository,
                           PublicacionService publicacionService,
                           UsuarioService usuarioService,
                           NotificacionService notificacionService) {
        this.reaccionRepository = reaccionRepository;
        this.publicacionService = publicacionService;
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
    }

    public List<Reaccion> obtenerPorPublicacion(Long publicacionId) {
        publicacionService.buscarPorId(publicacionId);
        return reaccionRepository.findByPublicacionId(publicacionId);
    }

    public Reaccion guardar(Reaccion reaccion) {
        validarReaccion(reaccion);
        Publicacion publicacion = publicacionService.buscarPorId(reaccion.getPublicacionId());
        Usuario usuario = usuarioService.buscarPorId(reaccion.getUsuarioId());
        Reaccion existente = reaccionRepository.findByPublicacionIdAndUsuarioId(publicacion.getId(), usuario.getId())
                .orElse(null);
        Reaccion guardada;
        if (existente != null) {
            existente.setTipo(reaccion.getTipo());
            guardada = reaccionRepository.save(existente);
        } else {
            reaccion.setUsuarioId(usuario.getId());
            guardada = reaccionRepository.save(reaccion);
            if (!publicacion.getUsuarioId().equals(usuario.getId())) {
                String mensaje = usuario.getNombre() + " reaccionó a tu publicación";
                notificacionService.crearNotificacion(publicacion.getUsuarioId(), mensaje, TipoNotificacion.REACCION);
            }
        }
        return guardada;
    }

    public void eliminar(Long publicacionId, Long usuarioId) {
        Reaccion reaccion = reaccionRepository.findByPublicacionIdAndUsuarioId(publicacionId, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reacción no encontrada"));
        reaccionRepository.deleteById(reaccion.getId());
    }

    public long contarPorTipo(Long publicacionId, TipoReaccion tipo) {
        return reaccionRepository.countByPublicacionIdAndTipo(publicacionId, tipo);
    }

    private void validarReaccion(Reaccion reaccion) {
        if (reaccion.getPublicacionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicación es obligatoria");
        }
        if (reaccion.getUsuarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario es obligatorio");
        }
        if (reaccion.getTipo() == null) {
            reaccion.setTipo(TipoReaccion.ME_GUSTA);
        }
    }
}
