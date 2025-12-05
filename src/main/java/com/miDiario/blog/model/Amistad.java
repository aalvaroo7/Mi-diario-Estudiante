package com.miDiario.blog.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
public class Amistad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private Usuario usuario1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private Usuario usuario2;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario1() { return usuario1; }
    public void setUsuario1(Usuario usuario1) { this.usuario1 = usuario1; }

    public Usuario getUsuario2() { return usuario2; }
    public void setUsuario2(Usuario usuario2) { this.usuario2 = usuario2; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}