package com.miDiario.blog.dto;

import com.miDiario.blog.model.Usuario;

public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String apellidos;
    private String nombreUsuario;
    private String email;
    private String genero;
    private String rol;
    private boolean activo;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Usuario usuario) {
        if (usuario == null) return;
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.apellidos = usuario.getApellidos();
        this.nombreUsuario = usuario.getNombreUsuario();
        this.email = usuario.getEmail();
        this.genero = usuario.getGenero();
        this.rol = usuario.getRol() != null ? usuario.getRol().getNombre() : null;
        this.activo = usuario.isActivo();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
