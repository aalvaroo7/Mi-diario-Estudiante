package com.miDiario.blog.service;

import com.miDiario.blog.dto.LoginDTO;
import com.miDiario.blog.dto.RegistroDTO;
import com.miDiario.blog.dto.UsuarioDTO;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.util.PasswordUtils;
import com.miDiario.blog.util.SessionManager;

import java.util.Map;
import java.util.Optional;

public class UsuarioService {
    private final DataStore dataStore;
    private final SessionManager sessionManager;

    public UsuarioService(DataStore dataStore, SessionManager sessionManager) {
        this.dataStore = dataStore;
        this.sessionManager = sessionManager;
    }

    public String registrar(RegistroDTO dto) {
        if (dto == null) return "Datos de registro vacíos";
        if (isBlank(dto.getNombre())) return "El nombre es obligatorio";
        if (isBlank(dto.getNombreUsuario())) return "El nombre de usuario es obligatorio";
        if (isBlank(dto.getCorreo())) return "El correo es obligatorio";
        if (isBlank(dto.getPassword())) return "La contraseña es obligatoria";

        Optional<Usuario> existingEmail = dataStore.findUsuarioByCorreo(dto.getCorreo());
        if (existingEmail.isPresent()) {
            return "El correo ya está registrado";
        }
        Optional<Usuario> existingUser = dataStore.findUsuarioByNombreUsuario(dto.getNombreUsuario());
        if (existingUser.isPresent()) {
            return "El nombre de usuario ya está en uso";
        }

        Usuario nuevo = new Usuario();
        nuevo.setId(dataStore.nextUserId());
        nuevo.setNombre(dto.getNombre());
        nuevo.setApellidos(dto.getApellidos());
        nuevo.setNombreUsuario(dto.getNombreUsuario());
        nuevo.setGenero(dto.getGenero());
        nuevo.setCorreo(dto.getCorreo());
        nuevo.setPasswordHash(PasswordUtils.hash(dto.getPassword()));
        nuevo.setRol("USUARIO");

        dataStore.getUsuarios().add(nuevo);
        return "Usuario registrado correctamente";
    }

    public Optional<LoginResult> login(LoginDTO dto, Map<String, String> headers) {
        if (dto == null || isBlank(dto.getIdentificador()) || isBlank(dto.getPassword())) {
            return Optional.empty();
        }

        Optional<Usuario> usuarioOpt = buscarPorIdentificador(dto.getIdentificador());
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();
        if (!usuario.isActivo()) {
            return Optional.empty();
        }

        String incomingHash = PasswordUtils.hash(dto.getPassword());
        if (!incomingHash.equals(usuario.getPasswordHash())) {
            return Optional.empty();
        }

        String sessionId = sessionManager.createSession(usuario.getId());
        headers.put("Set-Cookie", "SESSIONID=" + sessionId + "; Path=/; HttpOnly");
        return Optional.of(new LoginResult(new UsuarioDTO(usuario), sessionId));
    }

    public void logout(String sessionId) {
        sessionManager.destroy(sessionId);
    }

    public Optional<Usuario> buscarPorIdentificador(String identificador) {
        if (identificador == null) return Optional.empty();
        if (identificador.contains("@")) {
            return dataStore.findUsuarioByCorreo(identificador);
        }
        Optional<Usuario> byUsername = dataStore.findUsuarioByNombreUsuario(identificador);
        if (byUsername.isPresent()) return byUsername;
        return dataStore.getUsuarios().stream()
                .filter(u -> u.getNombre() != null && u.getNombre().equalsIgnoreCase(identificador))
                .findFirst();
    }

    public Optional<Usuario> findById(long id) {
        return dataStore.getUsuarios().stream().filter(u -> u.getId() == id).findFirst();
    }

    public Optional<Usuario> fromSession(String sessionId) {
        return sessionManager.getUserId(sessionId).flatMap(this::findById);
    }

    public record LoginResult(UsuarioDTO usuario, String sessionId) {}

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
