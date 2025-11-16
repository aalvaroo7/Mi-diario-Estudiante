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

import java.util.Optional;

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
    // BUSCAR POR EMAIL O NOMBRE (LOGIN FLEXIBLE)
    // ============================================================
    public Usuario buscarPorIdentificador(String identificador) {

        if (identificador == null || identificador.isBlank()) {
            return null;
        }

        // 1) Si contiene @ → email
        if (identificador.contains("@")) {
            return usuarioRepo.findByEmail(identificador);
        }

        // 2) Si coincide con nombre_usuario
        Usuario porUsername = usuarioRepo.findByNombreUsuario(identificador);
        if (porUsername != null) return porUsername;

        // 3) Si coincide con nombre real
        return usuarioRepo.findByNombre(identificador);
    }

    // ============================================================
    // REGISTRO DE USUARIOS
    // ============================================================
    public String registrar(RegistroDTO dto) {

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            return "El email es obligatorio";
        }

        if (dto.getNombreUsuario() == null || dto.getNombreUsuario().isBlank()) {
            return "El nombre de usuario es obligatorio";
        }

        if (usuarioRepo.existsByEmail(dto.getEmail())) {
            return "El correo ya está registrado";
        }

        if (usuarioRepo.existsByNombreUsuario(dto.getNombreUsuario())) {
            return "El nombre de usuario ya está en uso";
        }

        Rol rolUsuario = rolRepo.findByNombre("USUARIO");
        if (rolUsuario == null) {
            return "Rol USUARIO no configurado";
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(dto.getNombre());
        nuevo.setNombreUsuario(dto.getNombreUsuario());
        nuevo.setEmail(dto.getEmail());
        nuevo.setPassword(encoder.encode(dto.getPassword()));
        nuevo.setRol(rolUsuario);

        usuarioRepo.save(nuevo);

        registrarAuditoria(nuevo, "REGISTRO", true, "Usuario registrado correctamente");

        return "Usuario registrado correctamente";
    }

    // ============================================================
// LOGIN DEFINITIVO (FLEXIBLE + BCRYPT + AUTO-CIFRADO)
// ============================================================
    public String login(LoginDTO dto, HttpSession session) {

        if (dto == null || dto.getIdentificador() == null || dto.getIdentificador().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()) {
            registrarAuditoria(null, "LOGIN_FALLIDO", false, "Credenciales incompletas");
            return "Debes proporcionar usuario/email y contraseña";
        }

        // Buscar por email o nombreUsuario
        Usuario u;

        if (dto.getIdentificador().contains("@")) {
            u = usuarioRepo.findByEmail(dto.getIdentificador());
        } else {
            u = usuarioRepo.findByNombreUsuario(dto.getIdentificador());
        }

        if (u == null) {
            registrarAuditoria(null, "LOGIN_FALLIDO", false,
                    "Usuario/email no encontrado: " + dto.getIdentificador());
            return "Usuario no encontrado";
        }

        if (!u.isActivo()) {
            registrarAuditoria(u, "LOGIN_FALLIDO", false,
                    "Usuario bloqueado");
            return "Usuario bloqueado";
        }

        // ---- Obtener contraseñas ----
        String passwordBD = u.getPassword();
        String passwordLogin = dto.getPassword();

        boolean passwordCoincide;

        // ======================================================
        // 🔥 AUTO-CIFRADO DE CONTRASEÑAS EN TEXTO PLANO
        // ======================================================
        if (!passwordBD.startsWith("$2a$")) {

            String nuevaPasswordHash = encoder.encode(passwordBD);
            u.setPassword(nuevaPasswordHash);
            usuarioRepo.save(u);

            System.out.println("🔐 Contraseña de " + u.getNombre() +
                    " cifrada automáticamente (BCrypt).");
        }

        // ======================================================
        // VALIDACIÓN NORMAL CON BCRYPT
        // ======================================================
        passwordCoincide = encoder.matches(passwordLogin, u.getPassword());

        if (!passwordCoincide) {

            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            usuarioRepo.save(u);

            registrarAuditoria(u, "LOGIN_FALLIDO", false,
                    "Contraseña incorrecta");
            return "Contraseña incorrecta";
        }

        // ======================================================
        // LOGIN CORRECTO
        // ======================================================
        u.setIntentosFallidos(0);
        usuarioRepo.save(u);

        session.setAttribute("usuarioId", u.getId());
        session.setAttribute("rol", u.getRol().getNombre());

        registrarAuditoria(u, "LOGIN_EXITOSO", true,
                "Inicio de sesión correcto");

        return "Login correcto";
    }

    // ============================================================
    // LOGOUT
    // ============================================================
    public void logout(HttpSession session) {

        Long id = (Long) session.getAttribute("usuarioId");

        if (id != null) {
            usuarioRepo.findById(id).ifPresent(u -> registrarAuditoria(
                    u, "LOGOUT", true, "Cierre de sesión exitoso"));
        }

        session.invalidate();
    }

    // ============================================================
    // BLOQUEAR USUARIO (ADMIN)
    // ============================================================
    public ResponseEntity<?> bloquear(Long adminId, Long usuarioId) {

        Optional<Usuario> optAdmin = usuarioRepo.findById(adminId);
        Optional<Usuario> optUser = usuarioRepo.findById(usuarioId);

        if (optAdmin.isEmpty()) return ResponseEntity.status(404).body("Admin no encontrado");

        Usuario admin = optAdmin.get();

        if (!"ADMIN".equalsIgnoreCase(admin.getRol().getNombre()))
            return ResponseEntity.status(403).body("No tienes permisos");

        if (optUser.isEmpty()) return ResponseEntity.status(404).body("Usuario no encontrado");

        Usuario u = optUser.get();
        u.setActivo(false);
        usuarioRepo.save(u);

        registrarAuditoria(u, "BLOQUEADO_ADMIN", true,
                "Usuario bloqueado por admin: " + admin.getNombre());

        return ResponseEntity.ok("Usuario bloqueado correctamente");
    }

    // ============================================================
    // DESBLOQUEAR USUARIO (ADMIN)
    // ============================================================
    public ResponseEntity<?> desbloquear(Long adminId, Long usuarioId) {

        Optional<Usuario> optAdmin = usuarioRepo.findById(adminId);
        Optional<Usuario> optUser = usuarioRepo.findById(usuarioId);

        if (optAdmin.isEmpty()) return ResponseEntity.status(404).body("Admin no encontrado");

        Usuario admin = optAdmin.get();

        if (!"ADMIN".equalsIgnoreCase(admin.getRol().getNombre()))
            return ResponseEntity.status(403).body("No tienes permisos");

        if (optUser.isEmpty()) return ResponseEntity.status(404).body("Usuario no encontrado");

        Usuario u = optUser.get();
        u.setActivo(true);
        u.setIntentosFallidos(0);
        usuarioRepo.save(u);

        registrarAuditoria(u, "DESBLOQUEADO_ADMIN", true,
                "Usuario desbloqueado por admin: " + admin.getNombre());

        return ResponseEntity.ok("Usuario desbloqueado correctamente");
    }

    // ============================================================
    // AUDITORÍA
    // ============================================================
    private void registrarAuditoria(Usuario usuario, String accion, boolean exito, String detalles) {
        Auditoria a = new Auditoria();
        a.setUsuario(usuario);
        a.setAccion(accion);
        a.setExito(exito);
        a.setDetalles(detalles);
        auditoriaRepo.save(a);
    }
}
