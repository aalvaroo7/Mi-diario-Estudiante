package com.miDiario.blog.repository;

import com.miDiario.blog.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);
    boolean existsByNombreUsuario(String nombreUsuario);

    Usuario findByEmail(String email);
    Usuario findByNombreUsuario(String nombreUsuario);
}
