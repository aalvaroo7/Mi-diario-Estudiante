package com.MiDiarioEstudiante.backend.repository;

import com.MiDiarioEstudiante.backend.model.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    List<Publicacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}
