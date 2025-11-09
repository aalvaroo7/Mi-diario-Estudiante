package com.miDiario.blog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "publicaciones")
@Getter @Setter @NoArgsConstructor
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(columnDefinition = "LONGTEXT")
    private String imagenUrl;

    private LocalDateTime fechaPublicacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario autor;
}
