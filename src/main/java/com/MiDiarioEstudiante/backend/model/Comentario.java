package com.MiDiarioEstudiante.backend.model;

import java.time.LocalDateTime;

public class Comentario {
    private Long id;
    private Long publicacionId;
    private Long usuarioId;
    private String contenido;
    private LocalDateTime fechaCreacion;

    public Comentario() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Comentario(Long id, Long publicacionId, Long usuarioId, String contenido, LocalDateTime fechaCreacion) {
        this.id = id;
        this.publicacionId = publicacionId;
        this.usuarioId = usuarioId;
        this.contenido = contenido;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPublicacionId() {
        return publicacionId;
    }

    public void setPublicacionId(Long publicacionId) {
        this.publicacionId = publicacionId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaCreacion() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
