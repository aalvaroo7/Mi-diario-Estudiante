package com.MiDiarioEstudiante.service;

import com.MiDiarioEstudiante.http.HttpException;
import com.MiDiarioEstudiante.model.Usuario;
import com.MiDiarioEstudiante.repository.UsuarioRepository;

import java.util.List;

public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> HttpException.notFound("Usuario no encontrado"));
    }

    public Usuario registrar(Usuario usuario) {
        validarUsuario(usuario);
        usuarioRepository.findByEmail(usuario.getEmail()).ifPresent(existing -> {
            throw HttpException.conflict("El correo ya está registrado");
        });
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario usuarioActualizado) {
        Usuario existente = buscarPorId(id);
        if (hasText(usuarioActualizado.getNombre())) {
            existente.setNombre(usuarioActualizado.getNombre());
        }
        if (hasText(usuarioActualizado.getEmail()) && !usuarioActualizado.getEmail().equalsIgnoreCase(existente.getEmail())) {
            usuarioRepository.findByEmail(usuarioActualizado.getEmail()).ifPresent(conflict -> {
                if (!conflict.getId().equals(id)) {
                    throw HttpException.conflict("El correo ya está registrado");
                }
            });
            existente.setEmail(usuarioActualizado.getEmail());
        }
        if (hasText(usuarioActualizado.getPassword())) {
            existente.setPassword(usuarioActualizado.getPassword());
        }
        if (usuarioActualizado.getBiografia() != null) {
            existente.setBiografia(usuarioActualizado.getBiografia());
        }
        return usuarioRepository.save(existente);
    }

    public void eliminar(Long id) {
        buscarPorId(id);
        usuarioRepository.deleteById(id);
    }

    public Usuario autenticar(String email, String password) {
        if (!hasText(email) || !hasText(password)) {
            throw HttpException.badRequest("Correo y contraseña son obligatorios");
        }
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> HttpException.unauthorized("Credenciales inválidas"));
        if (!password.equals(usuario.getPassword())) {
            throw HttpException.unauthorized("Credenciales inválidas");
        }
        return usuario;
    }

    private void validarUsuario(Usuario usuario) {
        if (!hasText(usuario.getNombre())) {
            throw HttpException.badRequest("El nombre es obligatorio");
        }
        if (!hasText(usuario.getEmail())) {
            throw HttpException.badRequest("El correo es obligatorio");
        }
        if (!hasText(usuario.getPassword())) {
            throw HttpException.badRequest("La contraseña es obligatoria");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
