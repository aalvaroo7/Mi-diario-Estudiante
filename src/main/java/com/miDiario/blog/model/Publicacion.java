package com.miDiario.blog.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "publicaciones")
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usamos @Column(columnDefinition = "TEXT") o "LONGTEXT" para permitir textos muy largos (Base64)
    // En MySQL/MariaDB, "TEXT" suele ser suficiente para posts normales, pero para imágenes Base64
    // es mejor "LONGTEXT" para evitar que se corte si la imagen es grande.
    @Lob
    @Column(name = "imagen_url", columnDefinition = "LONGTEXT")
    private String imagenUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    // Relación con Usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // ==========================
    // CONSTRUCTORES
    // ==========================
    public Publicacion() {
    }

    public Publicacion(String contenido, Usuario usuario) {
        this.contenido = contenido;
        this.usuario = usuario;
        this.fechaPublicacion = LocalDateTime.now();
    }

    // ==========================
    // GETTERS Y SETTERS (¡Lo que faltaba!)
    // ==========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}