package com.MiDiarioEstudiante.repository;

import com.MiDiarioEstudiante.model.Publicacion;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PublicacionRepository {
    private final ConcurrentMap<Long, Publicacion> data = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public List<Publicacion> findAll() {
        List<Publicacion> publicaciones = new ArrayList<>(data.values());
        publicaciones.sort(Comparator.comparing(Publicacion::getFechaCreacion).reversed());
        return publicaciones;
    }

    public List<Publicacion> findByUsuarioId(Long usuarioId) {
        List<Publicacion> publicaciones = new ArrayList<>();
        for (Publicacion publicacion : data.values()) {
            if (usuarioId.equals(publicacion.getUsuarioId())) {
                publicaciones.add(publicacion);
            }
        }
        publicaciones.sort(Comparator.comparing(Publicacion::getFechaCreacion).reversed());
        return publicaciones;
    }

    public Optional<Publicacion> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    public Publicacion save(Publicacion publicacion) {
        if (publicacion.getId() == null) {
            publicacion.setId(sequence.getAndIncrement());
        }
        data.put(publicacion.getId(), publicacion);
        return publicacion;
    }

    public void deleteById(Long id) {
        data.remove(id);
    }
}
