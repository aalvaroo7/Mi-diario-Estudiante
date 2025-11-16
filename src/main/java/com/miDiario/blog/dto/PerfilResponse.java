package com.miDiario.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PerfilResponse {
    private Long id;
    private String nombre;
    private String apellidos;
    private String nombreUsuario;
    private String genero;
    private String correo;
    private String rol;
}
