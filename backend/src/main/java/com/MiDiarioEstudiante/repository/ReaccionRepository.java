package com.MiDiarioEstudiante.repository;

import com.MiDiarioEstudiante.model.Reaccion;
import com.MiDiarioEstudiante.model.enums.TipoReaccion;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ReaccionRepository {
    private final ConcurrentMap<Long, Reaccion> data = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public List<Reaccion> findByPublicacionId(Long publicacionId) {
        List<Reaccion> reacciones = new ArrayList<>();
        for (Reaccion reaccion : data.values()) {
            if (publicacionId.equals(reaccion.getPublicacionId())) {
                reacciones.add(reaccion);
            }
        }
        return reacciones;
    }

    public Optional<Reaccion> findByPublicacionIdAndUsuarioId(Long publicacionId, Long usuarioId) {
        return data.values().stream()
                .filter(reaccion -> publicacionId.equals(reaccion.getPublicacionId())
                        && usuarioId.equals(reaccion.getUsuarioId()))
                .findFirst();
    }

    public long countByPublicacionIdAndTipo(Long publicacionId, TipoReaccion tipo) {
        return data.values().stream()
                .filter(reaccion -> publicacionId.equals(reaccion.getPublicacionId()) && tipo == reaccion.getTipo())
                .count();
    }

    public Reaccion save(Reaccion reaccion) {
        if (reaccion.getId() == null) {
            reaccion.setId(sequence.getAndIncrement());
        }
        data.put(reaccion.getId(), reaccion);
        return reaccion;
    }

    public void deleteById(Long id) {
        data.remove(id);
    }
}
