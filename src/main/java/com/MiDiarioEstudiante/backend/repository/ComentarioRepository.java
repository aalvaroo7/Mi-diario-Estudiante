package com.MiDiarioEstudiante.backend.repository;

import com.MiDiarioEstudiante.backend.model.Comentario;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ComentarioRepository {
    private final ConcurrentMap<Long, Comentario> data = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public List<Comentario> findByPublicacionId(Long publicacionId) {
        List<Comentario> comentarios = new ArrayList<>();
        for (Comentario comentario : data.values()) {
            if (publicacionId.equals(comentario.getPublicacionId())) {
                comentarios.add(comentario);
            }
        }
        comentarios.sort(Comparator.comparing(Comentario::getFechaCreacion));
        return comentarios;
    }

    public Optional<Comentario> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    public Comentario save(Comentario comentario) {
        if (comentario.getId() == null) {
            comentario.setId(sequence.getAndIncrement());
        }
        data.put(comentario.getId(), comentario);
        return comentario;
    }

    public void deleteById(Long id) {
        data.remove(id);
    }
}
