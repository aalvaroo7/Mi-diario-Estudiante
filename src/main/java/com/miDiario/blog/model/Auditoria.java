package com.miDiario.blog.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario implicado (puede ser null)
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Acción realizada
    @Column(nullable = false)
    private String accion;

    // IP desde donde se hizo la acción (opcional)
    @Column
    private String ip;

    // Si la acción fue exitosa o fallida
    @Column(nullable = false)
    private boolean exito;

    // Fecha de la acción
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    // Detalles adicionales
    @Column(columnDefinition = "TEXT")
    private String detalles;
}
