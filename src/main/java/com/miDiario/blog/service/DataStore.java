package com.miDiario.blog.service;

import com.miDiario.blog.model.Publicacion;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.util.PasswordUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class DataStore {
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Publicacion> publicaciones = new ArrayList<>();
    private final AtomicLong userId = new AtomicLong(1);
    private final AtomicLong postId = new AtomicLong(1);

    public DataStore() {
        // Usuario de demostración para poder iniciar sesión inmediatamente
        Usuario demo = new Usuario(
                userId.getAndIncrement(),
                "Estudiante Demo",
                "", "demo", "No binario",
                "demo@correo.com",
                PasswordUtils.hash("demo123"),
                "ADMIN"
        );
        usuarios.add(demo);

        Publicacion pub = new Publicacion(postId.getAndIncrement(),
                "Bienvenido a Mi Diario Estudiante. Puedes publicar tu primer mensaje.", demo);
        pub.setFechaPublicacion(LocalDateTime.now());
        publicaciones.add(pub);
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Publicacion> getPublicaciones() {
        return publicaciones;
    }

    public long nextUserId() {
        return userId.getAndIncrement();
    }

    public long nextPostId() {
        return postId.getAndIncrement();
    }

    public Optional<Usuario> findUsuarioByCorreo(String correo) {
        return usuarios.stream()
                .filter(u -> u.getCorreo() != null && u.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
    }

    public Optional<Usuario> findUsuarioByNombreUsuario(String nombreUsuario) {
        return usuarios.stream()
                .filter(u -> u.getNombreUsuario() != null && u.getNombreUsuario().equalsIgnoreCase(nombreUsuario))
                .findFirst();
    }
}
