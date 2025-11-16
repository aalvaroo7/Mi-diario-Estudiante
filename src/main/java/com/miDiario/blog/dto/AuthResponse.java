package com.miDiario.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private Long usuarioId;
    private String nombreUsuario;
    private String rol;
    private String mensaje;
}
