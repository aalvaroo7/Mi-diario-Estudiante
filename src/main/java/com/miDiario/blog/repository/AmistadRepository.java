package com.miDiario.blog.repository;

import com.miDiario.blog.model.Amistad;
import com.miDiario.blog.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmistadRepository extends JpaRepository<Amistad, Long> {

    // Buscar amistad entre dos usuarios
    @Query("SELECT a FROM Amistad a WHERE " +
            "(a.usuario1 = :usuario1 AND a.usuario2 = :usuario2) OR " +
            "(a.usuario1 = :usuario2 AND a.usuario2 = :usuario1)")
    Optional<Amistad> findByUsuarios(@Param("usuario1") Usuario usuario1,
                                     @Param("usuario2") Usuario usuario2);

    // Obtener todas las amistades de un usuario
    @Query("SELECT a FROM Amistad a WHERE " +
            "a.usuario1 = :usuario OR a.usuario2 = :usuario")
    List<Amistad> findByUsuario(@Param("usuario") Usuario usuario);

    // Verificar si dos usuarios son amigos
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Amistad a WHERE " +
            "(a.usuario1 = :usuario1 AND a.usuario2 = :usuario2) OR " +
            "(a.usuario1 = :usuario2 AND a.usuario2 = :usuario1)")
    boolean sonAmigos(@Param("usuario1") Usuario usuario1,
                      @Param("usuario2") Usuario usuario2);
}