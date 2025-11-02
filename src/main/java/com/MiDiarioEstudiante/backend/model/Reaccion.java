package com.MiDiarioEstudiante.backend.model;

import com.MiDiarioEstudiante.backend.model.enums.TipoReaccion;

import java.time.LocalDateTime;

public class Reaccion {
    private Long id;
    private Long publicacionId;
    private Long usuarioId;
    private TipoReaccion tipo;
    private LocalDateTime fechaCreacion;

    public Reaccion() {
        this.fechaCreacion = LocalDateTime.now();
        this.tipo = TipoReaccion.ME_GUSTA;
    }

    public Reaccion(Long id, Long publicacionId, Long usuarioId, TipoReaccion tipo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.publicacionId = publicacionId;
        this.usuarioId = usuarioId;
        this.tipo = tipo != null ? tipo : TipoReaccion.ME_GUSTA;
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

    public TipoReaccion getTipo() {
        if (tipo == null) {
            tipo = TipoReaccion.ME_GUSTA;
        }
        return tipo;
    }

    public void setTipo(TipoReaccion tipo) {
        this.tipo = tipo;
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
