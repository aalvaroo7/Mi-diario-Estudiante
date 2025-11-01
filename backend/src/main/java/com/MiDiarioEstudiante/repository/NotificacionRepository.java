package com.MiDiarioEstudiante.repository;

import com.MiDiarioEstudiante.model.Notificacion;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class NotificacionRepository {
    private final ConcurrentMap<Long, Notificacion> data = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public List<Notificacion> findByUsuarioId(Long usuarioId) {
        List<Notificacion> notificaciones = new ArrayList<>();
        for (Notificacion notificacion : data.values()) {
            if (usuarioId.equals(notificacion.getUsuarioId())) {
                notificaciones.add(notificacion);
            }
        }
        notificaciones.sort(Comparator.comparing(Notificacion::getFechaCreacion).reversed());
        return notificaciones;
    }

    public Optional<Notificacion> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    public Notificacion save(Notificacion notificacion) {
        if (notificacion.getId() == null) {
            notificacion.setId(sequence.getAndIncrement());
        }
        data.put(notificacion.getId(), notificacion);
        return notificacion;
    }
}
