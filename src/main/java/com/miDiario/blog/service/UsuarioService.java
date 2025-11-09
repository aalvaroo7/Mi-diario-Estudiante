package com.miDiario.blog.service;

import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public String registrarUsuario(Usuario usuario) {
        if (usuarioRepository.findByNombreUsuario(usuario.getNombreUsuario()).isPresent()) {
            return "Error: El nombre de usuario ya existe.";
        }
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            return "Error: El correo ya está registrado.";
        }
        usuarioRepository.save(usuario);
        return "Usuario registrado correctamente.";
    }

    public Optional<Usuario> login(String nombreUsuario, String password) {
        return usuarioRepository.findByNombreUsuarioAndPassword(nombreUsuario, password);
    }
}
