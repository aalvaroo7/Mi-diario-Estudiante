package com.miDiario.blog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.miDiario.blog.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
        Optional<Usuario> findByNombreUsuario(String nombreUsuario);
        Optional<Usuario> findByCorreo(String correo);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true)
    private String nombreUsuario;

    @Column(nullable = false)
    private String genero;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol;
}
