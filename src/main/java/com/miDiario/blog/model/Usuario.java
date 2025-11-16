package com.miDiario.blog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre visible del usuario
    @Column(nullable = false)
    private String nombre;

    @Column
    private String apellidos;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column
    private String genero;

    // Email mapeado a la columna `correo` de la BD
    @Column(name = "correo", nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // Nº de intentos fallidos de login
    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos = 0;

    // Estado del usuario (activo/bloqueado)
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    // Rol (USUARIO, ADMIN, TECNICO) vía tabla roles
    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;
}
