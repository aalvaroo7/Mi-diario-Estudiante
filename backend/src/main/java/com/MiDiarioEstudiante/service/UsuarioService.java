package com.MiDiarioEstudiante.service;

import com.MiDiarioEstudiante.model.Usuario;
import com.MiDiarioEstudiante.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario registrar(Usuario usuario) {
        validarUsuario(usuario);
        usuarioRepository.findByEmail(usuario.getEmail()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        });
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario usuarioActualizado) {
        Usuario existente = buscarPorId(id);
        if (StringUtils.hasText(usuarioActualizado.getNombre())) {
            existente.setNombre(usuarioActualizado.getNombre());
        }
        if (StringUtils.hasText(usuarioActualizado.getEmail()) && !usuarioActualizado.getEmail().equalsIgnoreCase(existente.getEmail())) {
            usuarioRepository.findByEmail(usuarioActualizado.getEmail()).ifPresent(conflict -> {
                if (!conflict.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
                }
            });
            existente.setEmail(usuarioActualizado.getEmail());
        }
        if (StringUtils.hasText(usuarioActualizado.getPassword())) {
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
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo y contraseña son obligatorios");
        }
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        if (!password.equals(usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        return usuario;
    }

    private void validarUsuario(Usuario usuario) {
        if (!StringUtils.hasText(usuario.getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (!StringUtils.hasText(usuario.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo es obligatorio");
        }
        if (!StringUtils.hasText(usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
    }
}
