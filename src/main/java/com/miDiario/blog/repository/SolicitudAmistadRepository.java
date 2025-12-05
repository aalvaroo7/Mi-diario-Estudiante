package com.miDiario.blog.repository;

import com.miDiario.blog.model.SolicitudAmistad;
import com.miDiario.blog.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudAmistadRepository extends JpaRepository<SolicitudAmistad, Long> {

    // Buscar solicitud entre dos usuarios
    Optional<SolicitudAmistad> findBySolicitanteAndDestinatarioAndStatus(
            Usuario solicitante, Usuario destinatario, SolicitudAmistad.EstadoSolicitud status);

    // Solicitudes pendientes recibidas por un usuario
    List<SolicitudAmistad> findByDestinatarioAndStatus(
            Usuario destinatario, SolicitudAmistad.EstadoSolicitud status);

    // Solicitudes enviadas por un usuario
    List<SolicitudAmistad> findBySolicitanteAndStatus(
            Usuario solicitante, SolicitudAmistad.EstadoSolicitud status);

    // Verificar si existe solicitud pendiente entre dos usuarios
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM SolicitudAmistad s WHERE " +
            "((s.solicitante = :usuario1 AND s.destinatario = :usuario2) OR " +
            "(s.solicitante = :usuario2 AND s.destinatario = :usuario1)) AND " +
            "s.status = 'PENDIENTE'")
    boolean existsSolicitudPendiente(@Param("usuario1") Usuario usuario1,
                                     @Param("usuario2") Usuario usuario2);
}