package com.miDiario.blog.repository;

import com.miDiario.blog.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // BUSCAR POR EMAIL (importante: la variable en Usuario.java se llama 'email')
    Optional<Usuario> findByEmail(String email);

    // BUSCAR POR NOMBRE DE USUARIO
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    // BUSCAR POR IDENTIFICADOR (email o nombre de usuario)
    @Query("SELECT u FROM Usuario u WHERE u.nombreUsuario = :identificador OR u.email = :identificador")
    Optional<Usuario> findByIdentificador(@Param("identificador") String identificador);

    // VERIFICAR EXISTENCIA POR EMAIL
    boolean existsByEmail(String email);

    // VERIFICAR EXISTENCIA POR NOMBRE DE USUARIO
    boolean existsByNombreUsuario(String nombreUsuario);

    // BUSCAR USUARIOS PARA EL BUSCADOR DE AMIGOS
    @Query("SELECT u FROM Usuario u WHERE " +
            "(LOWER(u.nombre) LIKE LOWER(:termino) OR " +
            "LOWER(u.apellidos) LIKE LOWER(:termino) OR " +
            "LOWER(u.nombreUsuario) LIKE LOWER(:termino) OR " +
            "LOWER(u.email) LIKE LOWER(:termino)) AND " +
            "u.id <> :excluidoId AND u.activo = true")
    List<Usuario> buscarPorTermino(@Param("termino") String termino,
                                   @Param("excluidoId") Long excluidoId);
}