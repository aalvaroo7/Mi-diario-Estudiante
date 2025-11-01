package com.MiDiarioEstudiante.backend.dto;

import com.MiDiarioEstudiante.backend.model.enums.TipoVisibilidad;

public class PublicacionRequest {
    private Long usuarioId;
    private String contenido;
    private TipoVisibilidad visibilidad;

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

    public TipoVisibilidad getVisibilidad() {
        return visibilidad;
    }

    public void setVisibilidad(TipoVisibilidad visibilidad) {
        this.visibilidad = visibilidad;
    }
}
