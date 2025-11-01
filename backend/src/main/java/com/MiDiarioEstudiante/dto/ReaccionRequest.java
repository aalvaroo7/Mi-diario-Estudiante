package com.MiDiarioEstudiante.dto;

import com.MiDiarioEstudiante.model.enums.TipoReaccion;

public class ReaccionRequest {
    private Long publicacionId;
    private Long usuarioId;
    private TipoReaccion tipo;

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
        return tipo;
    }

    public void setTipo(TipoReaccion tipo) {
        this.tipo = tipo;
    }
}
