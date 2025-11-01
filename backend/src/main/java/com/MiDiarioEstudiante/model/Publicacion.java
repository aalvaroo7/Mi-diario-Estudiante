package com.MiDiarioEstudiante.model;

import com.MiDiarioEstudiante.model.enums.TipoVisibilidad;

import java.time.LocalDateTime;

public class Publicacion {
    private Long id;
    private Long usuarioId;
    private String contenido;
    private LocalDateTime fechaCreacion;
    private TipoVisibilidad visibilidad;

    public Publicacion() {
        this.fechaCreacion = LocalDateTime.now();
        this.visibilidad = TipoVisibilidad.PUBLICA;
    }

    public Publicacion(Long id, Long usuarioId, String contenido, LocalDateTime fechaCreacion, TipoVisibilidad visibilidad) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.contenido = contenido;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.visibilidad = visibilidad != null ? visibilidad : TipoVisibilidad.PUBLICA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public TipoVisibilidad getVisibilidad() {
        if (visibilidad == null) {
            visibilidad = TipoVisibilidad.PUBLICA;
        }
        return visibilidad;
    }

    public void setVisibilidad(TipoVisibilidad visibilidad) {
        this.visibilidad = visibilidad;
    }
}
