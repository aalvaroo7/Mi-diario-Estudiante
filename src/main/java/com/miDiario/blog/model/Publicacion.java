package com.miDiario.blog.model;

import java.time.LocalDateTime;

public class Publicacion {
    private long id;
    private String contenido;
    private LocalDateTime fechaPublicacion = LocalDateTime.now();
    private Usuario autor;

    public Publicacion() {
    }

    public Publicacion(long id, String contenido, Usuario autor) {
        this.id = id;
        this.contenido = contenido;
        this.autor = autor;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }
}
