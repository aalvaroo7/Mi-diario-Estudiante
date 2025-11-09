package com.miDiario.blog.repository;

import com.miDiario.blog.model.Publicacion;
import com.miDiario.blog.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    List<Publicacion> findByAutor(Usuario autor);
}
