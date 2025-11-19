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
    // BUSCAR POR IDENTIFICADOR (LOGIN FLEXIBLE)
    // ============================================================
    public Usuario buscarPorIdentificador(String identificador) {

        // Si contiene @ → lo tratamos como email
        if (identificador != null && identificador.contains("@")) {
            return usuarioRepo.findByEmail(identificador);
        }

        // En otro caso → nombre de usuario
        return usuarioRepo.findByNombreUsuario(identificador);
    }

    // ============================================================
    // REGISTRO
    // ============================================================
    public String registrar(RegistroDTO dto) {

        // Email único
        if (usuarioRepo.existsByEmail(dto.getEmail())) {
            return "El correo ya está registrado";
        }

        // Nombre de usuario único
        if (usuarioRepo.existsByNombreUsuario(dto.getNombreUsuario())) {
            return "El nombre de usuario ya está en uso";
        }

        // Longitud mínima contraseña
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres";
        }

        // Rol por defecto USUARIO
        Rol rolUsuario = rolRepo.findByNombre("USUARIO");
        if (rolUsuario == null) {
            throw new RuntimeException("ERROR: El rol 'USUARIO' no existe en la base de datos");
        }

        // Construir entidad Usuario
        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setNombreUsuario(dto.getNombreUsuario());
        u.setEmail(dto.getEmail());
        // Campos opcionales (no vienen en el DTO)
        u.setApellidos(null);
        u.setGenero(null);

        // Cifrado de contraseña
        u.setPassword(encoder.encode(dto.getPassword()));

        u.setRol(rolUsuario);
        u.setActivo(true);
        u.setIntentosFallidos(0);

        usuarioRepo.save(u);

        // Auditoría
        registrarAuditoria(u, "REGISTRO_USUARIO", true,
                "Usuario registrado correctamente");

        return "Usuario registrado correctamente";
    }

    // ============================================================
    // LOGIN (MIXTO + BCRYPT + AUTO-CIFRADO)
    // ============================================================
    public String login(LoginDTO dto, HttpSession session) {

        Usuario u = buscarPorIdentificador(dto.getIdentificador());

        // Usuario no encontrado
        if (u == null) {
            registrarAuditoria(null, "LOGIN_FALLIDO", false,
                    "Usuario/email no encontrado: " + dto.getIdentificador());
            return "Usuario no encontrado";
        }

        // Usuario bloqueado
        if (!u.isActivo()) {
            registrarAuditoria(u, "LOGIN_FALLIDO", false,
                    "Usuario bloqueado");
            return "Usuario bloqueado";
        }

        String passwordBD = u.getPassword();
        String passwordLogin = dto.getPassword();

        // ======================================================
        // AUTO-CIFRADO DE CONTRASEÑAS LEGACY (texto plano)
        // ======================================================
        if (passwordBD != null && !passwordBD.startsWith("$2a$")) {
            String nuevaPasswordHash = encoder.encode(passwordBD);
            u.setPassword(nuevaPasswordHash);
            usuarioRepo.save(u);
        }

        // Validación con BCrypt
        boolean passwordCoincide = encoder.matches(passwordLogin, u.getPassword());

        if (!passwordCoincide) {
            // Incrementar intentos fallidos
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            usuarioRepo.save(u);

            registrarAuditoria(u, "LOGIN_FALLIDO", false,
                    "Contraseña incorrecta");
            return "Contraseña incorrecta";
        }

        // LOGIN CORRECTO
        u.setIntentosFallidos(0);
        usuarioRepo.save(u);

        // Guardar datos mínimos en sesión
        session.setAttribute("usuarioId", u.getId());
        session.setAttribute("nombre", u.getNombre());
        // Guardamos el nombre del rol (ADMIN, USUARIO, etc.)
        session.setAttribute("rol", u.getRol() != null ? u.getRol().getNombre() : "USUARIO");

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
            usuarioRepo.findById(id).ifPresent(u ->
                    registrarAuditoria(u, "LOGOUT", true,
                            "Cierre de sesión"));
        }

        session.invalidate();
    }

    // ============================================================
    // BLOQUEAR USUARIO (para AdminController / UsuarioController)
    // ============================================================
    public ResponseEntity<?> bloquear(Long adminId, Long usuarioId) {

        Usuario admin = usuarioRepo.findById(adminId).orElse(null);

        if (admin == null) {
            return ResponseEntity.status(404).body("Administrador no encontrado");
        }

        if (admin.getRol() == null ||
                !"ADMIN".equalsIgnoreCase(admin.getRol().getNombre())) {
            return ResponseEntity.status(403).body("No tienes permisos de administrador");
        }

        Usuario u = usuarioRepo.findById(usuarioId).orElse(null);

        if (u == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        u.setActivo(false);
        usuarioRepo.save(u);

        registrarAuditoria(u, "BLOQUEADO_ADMIN", true,
                "Usuario bloqueado por admin: " + admin.getNombre());

        return ResponseEntity.ok("Usuario bloqueado correctamente");
    }

    // ============================================================
    // DESBLOQUEAR USUARIO
    // ============================================================
    public ResponseEntity<?> desbloquear(Long adminId, Long usuarioId) {

        Usuario admin = usuarioRepo.findById(adminId).orElse(null);

        if (admin == null) {
            return ResponseEntity.status(404).body("Administrador no encontrado");
        }

        if (admin.getRol() == null ||
                !"ADMIN".equalsIgnoreCase(admin.getRol().getNombre())) {
            return ResponseEntity.status(403).body("No tienes permisos de administrador");
        }

        Usuario u = usuarioRepo.findById(usuarioId).orElse(null);

        if (u == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

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
    private void registrarAuditoria(Usuario usuario,
                                    String accion,
                                    boolean exito,
                                    String detalles) {

        Auditoria audit = new Auditoria();
        audit.setUsuario(usuario);
        audit.setAccion(accion);
        audit.setExito(exito);
        audit.setDetalles(detalles);

        auditoriaRepo.save(audit);
    }
}
