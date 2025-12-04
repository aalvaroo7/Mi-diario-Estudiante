package com.miDiario.blog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios") // Tu tabla en la BD
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String apellidos;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column
    private String genero;

    @Column(name = "correo", nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos = 0;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    // --- NUEVO CAMPO PARA LA FOTO ---
    @Column(name = "foto_perfil", columnDefinition = "LONGTEXT")
    private String fotoPerfil;
    // -------------------------------

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;
}
