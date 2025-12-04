package com.miDiario.blog.service;

import com.miDiario.blog.dto.LoginDTO;
import com.miDiario.blog.dto.RegistroDTO;
import com.miDiario.blog.model.Auditoria;
import com.miDiario.blog.model.Rol;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.repository.AuditoriaRepository;
import com.miDiario.blog.repository.RolRepository;
import com.miDiario.blog.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final AuditoriaRepository auditoriaRepo;
    private final BCryptPasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarioRepo,
                          RolRepository rolRepo,
                          AuditoriaRepository auditoriaRepo,
                          BCryptPasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.rolRepo = rolRepo;
        this.auditoriaRepo = auditoriaRepo;
        this.encoder = encoder;
    }

    // ============================================================
    // ACTUALIZAR PERFIL (TU CÓDIGO: INCLUYE FOTO Y DATOS)
    // ============================================================
    public Usuario actualizar(Long id, Usuario datosNuevos) {
        // 1. Buscar usuario existente
        Usuario usuarioExistente = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // --- ACTUALIZAR DATOS PERSONALES ---
        if (datosNuevos.getNombre() != null && !datosNuevos.getNombre().isEmpty()) {
            usuarioExistente.setNombre(datosNuevos.getNombre());
        }

        if (datosNuevos.getApellidos() != null) {
            usuarioExistente.setApellidos(datosNuevos.getApellidos());
        }

        if (datosNuevos.getGenero() != null) {
            usuarioExistente.setGenero(datosNuevos.getGenero());
        }

        // --- ¡CLAVE! GUARDAR FOTO DE PERFIL ---
        if (datosNuevos.getFotoPerfil() != null && !datosNuevos.getFotoPerfil().isEmpty()) {
            usuarioExistente.setFotoPerfil(datosNuevos.getFotoPerfil());
        }

        // --- VALIDACIONES DE SEGURIDAD (Nick y Email) ---
        if (datosNuevos.getNombreUsuario() != null && !datosNuevos.getNombreUsuario().isEmpty()) {
            if (!datosNuevos.getNombreUsuario().equals(usuarioExistente.getNombreUsuario()) &&
                    usuarioRepo.existsByNombreUsuario(datosNuevos.getNombreUsuario())) {
                throw new RuntimeException("El nombre de usuario ya está ocupado.");
            }
            usuarioExistente.setNombreUsuario(datosNuevos.getNombreUsuario());
        }

        if (datosNuevos.getEmail() != null && !datosNuevos.getEmail().isEmpty()) {
            if (!datosNuevos.getEmail().equals(usuarioExistente.getEmail()) &&
                    usuarioRepo.existsByEmail(datosNuevos.getEmail())) {
                throw new RuntimeException("El correo ya está registrado.");
            }
            usuarioExistente.setEmail(datosNuevos.getEmail());
        }

        // Guardar y Auditar
        Usuario guardado = usuarioRepo.save(usuarioExistente);
        registrarAuditoria(guardado, "PERFIL_ACTUALIZADO", true, "Usuario actualizó perfil/foto");
        return guardado;
    }

    // ============================================================
    // REGISTRO (CÓDIGO FUSIONADO CON EL DE TU AMIGO)
    // ============================================================
    public String registrar(RegistroDTO dto) {
        if (usuarioRepo.existsByEmail(dto.getEmail())) {
            return "El correo ya está registrado";
        }
        if (usuarioRepo.existsByNombreUsuario(dto.getNombreUsuario())) {
            return "El nombre de usuario ya está en uso";
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres";
        }

        Rol rolUsuario = rolRepo.findByNombre("USUARIO");
        if (rolUsuario == null) {
            // Esto es importante tenerlo controlado
            throw new RuntimeException("ERROR CRÍTICO: El rol 'USUARIO' no existe en la base de datos.");
        }

        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setNombreUsuario(dto.getNombreUsuario());
        u.setEmail(dto.getEmail());
        u.setPassword(encoder.encode(dto.getPassword()));

        // --- AQUÍ ESTÁ EL CAMBIO DE TU AMIGO ---
        // Ahora recogemos apellidos y género desde el registro, ya no son null
        u.setApellidos(dto.getApellidos());
        u.setGenero(dto.getGenero());
        // ---------------------------------------

        u.setRol(rolUsuario);
        u.setActivo(true);
        u.setIntentosFallidos(0);

        usuarioRepo.save(u);
        registrarAuditoria(u, "REGISTRO_USUARIO", true, "Usuario registrado correctamente");

        return "Usuario registrado correctamente";
    }

    // ============================================================
    // LOGIN (SEGURO Y AUDITADO)
    // ============================================================
    public String login(LoginDTO dto, HttpSession session) {
        Usuario u = buscarPorIdentificador(dto.getIdentificador());

        if (u == null) {
            registrarAuditoria(null, "LOGIN_FALLIDO", false, "Usuario no encontrado: " + dto.getIdentificador());
            return "Usuario no encontrado";
        }

        if (!u.isActivo()) {
            registrarAuditoria(u, "LOGIN_FALLIDO", false, "Usuario bloqueado");
            return "Usuario bloqueado";
        }

        // Compatibilidad con contraseñas antiguas (texto plano)
        String passwordBD = u.getPassword();
        if (passwordBD != null && !passwordBD.startsWith("$2a$")) {
            u.setPassword(encoder.encode(passwordBD));
            usuarioRepo.save(u);
        }

        if (!encoder.matches(dto.getPassword(), u.getPassword())) {
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            usuarioRepo.save(u);
            registrarAuditoria(u, "LOGIN_FALLIDO", false, "Contraseña incorrecta");
            return "Contraseña incorrecta";
        }

        // Login exitoso
        u.setIntentosFallidos(0);
        usuarioRepo.save(u);

        session.setAttribute("usuarioId", u.getId());
        session.setAttribute("nombre", u.getNombre());
        session.setAttribute("rol", u.getRol() != null ? u.getRol().getNombre() : "USUARIO");

        registrarAuditoria(u, "LOGIN_EXITOSO", true, "Inicio de sesión correcto");
        return "Login correcto";
    }

    // ============================================================
    // LOGOUT
    // ============================================================
    public void logout(HttpSession session) {
        Long id = (Long) session.getAttribute("usuarioId");
        if (id != null) {
            usuarioRepo.findById(id).ifPresent(u ->
                    registrarAuditoria(u, "LOGOUT", true, "Cierre de sesión"));
        }
        session.invalidate();
    }

    // ============================================================
    // MÉTODOS AUXILIARES Y ADMIN
    // ============================================================
    public Usuario buscarPorIdentificador(String identificador) {
        if (identificador != null && identificador.contains("@")) {
            return usuarioRepo.findByEmail(identificador);
        }
        return usuarioRepo.findByNombreUsuario(identificador);
    }

    public ResponseEntity<?> bloquear(Long adminId, Long usuarioId) {
        if (!esAdmin(adminId)) return ResponseEntity.status(403).body("No tienes permisos de administrador");

        Usuario u = usuarioRepo.findById(usuarioId).orElse(null);
        if (u == null) return ResponseEntity.status(404).body("Usuario no encontrado");

        u.setActivo(false);
        usuarioRepo.save(u);
        registrarAuditoria(u, "BLOQUEADO_ADMIN", true, "Bloqueado por admin ID: " + adminId);
        return ResponseEntity.ok("Usuario bloqueado correctamente");
    }

    public ResponseEntity<?> desbloquear(Long adminId, Long usuarioId) {
        if (!esAdmin(adminId)) return ResponseEntity.status(403).body("No tienes permisos de administrador");

        Usuario u = usuarioRepo.findById(usuarioId).orElse(null);
        if (u == null) return ResponseEntity.status(404).body("Usuario no encontrado");

        u.setActivo(true);
        u.setIntentosFallidos(0);
        usuarioRepo.save(u);
        registrarAuditoria(u, "DESBLOQUEADO_ADMIN", true, "Desbloqueado por admin ID: " + adminId);
        return ResponseEntity.ok("Usuario desbloqueado correctamente");
    }

    private boolean esAdmin(Long id) {
        Usuario admin = usuarioRepo.findById(id).orElse(null);
        return admin != null && admin.getRol() != null && "ADMIN".equalsIgnoreCase(admin.getRol().getNombre());
    }

    private void registrarAuditoria(Usuario usuario, String accion, boolean exito, String detalles) {
        Auditoria audit = new Auditoria();
        audit.setUsuario(usuario);
        audit.setAccion(accion);
        audit.setExito(exito);
        audit.setDetalles(detalles);
        auditoriaRepo.save(audit);
    }
}