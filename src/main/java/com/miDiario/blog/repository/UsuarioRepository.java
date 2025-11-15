package com.miDiario.blog.repository;

import com.miDiario.blog.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    boolean existsByNombre(String nombre);

    Usuario findByEmail(String email);

    // Necesarios para login con email o usuario
    java.util.Optional<Usuario> findByCorreo(String correo);

    java.util.Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}

