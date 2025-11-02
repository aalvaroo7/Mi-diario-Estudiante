package com.MiDiarioEstudiante.backend.repository;

import com.MiDiarioEstudiante.backend.model.Reaccion;
import com.MiDiarioEstudiante.backend.model.enums.TipoReaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReaccionRepository extends JpaRepository<Reaccion, Long> {
    List<Reaccion> findByPublicacionId(Long publicacionId);

    Optional<Reaccion> findByPublicacionIdAndUsuarioId(Long publicacionId, Long usuarioId);

    long countByPublicacionIdAndTipo(Long publicacionId, TipoReaccion tipo);
}
