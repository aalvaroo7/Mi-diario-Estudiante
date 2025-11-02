package com.MiDiarioEstudiante.backend.model;

import com.MiDiarioEstudiante.backend.model.enums.TipoNotificacion;

import java.time.LocalDateTime;

public class Notificacion {
    private Long id;
    private Long usuarioId;
    private String mensaje;
    private TipoNotificacion tipo;
    private boolean leido;
    private LocalDateTime fechaCreacion;

    public Notificacion() {
        this.fechaCreacion = LocalDateTime.now();
        this.tipo = TipoNotificacion.GENERAL;
    }

    public Notificacion(Long id, Long usuarioId, String mensaje, TipoNotificacion tipo, boolean leido, LocalDateTime fechaCreacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.mensaje = mensaje;
        this.tipo = tipo != null ? tipo : TipoNotificacion.GENERAL;
        this.leido = leido;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public TipoNotificacion getTipo() {
        if (tipo == null) {
            tipo = TipoNotificacion.GENERAL;
        }
        return tipo;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
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
