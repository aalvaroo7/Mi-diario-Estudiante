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
    // MÉTODO PRIVADO PARA REGISTRAR AUDITORÍA (RNF-13)
    // ============================================================
    private void registrarAuditoria(Usuario usuario, String accion, boolean exito, String detalles) {
        Auditoria a = new Auditoria();
        a.setUsuario(usuario);
        a.setAccion(accion);
        a.setExito(exito);
        a.setDetalles(detalles);
        auditoriaRepo.save(a);
    }


    // ============================================================
    // REGISTRO (RF-01 + RNF-01 + RNF-14)
    // ============================================================
    public String registrar(RegistroDTO dto) {

        if (usuarioRepo.existsByEmail(dto.getEmail())) {
            registrarAuditoria(null, "REGISTRO_FALLIDO", false,
                    "Email duplicado: " + dto.getEmail());
            return "El email ya está registrado";
        }

        if (usuarioRepo.existsByNombre(dto.getNombre())) {
            registrarAuditoria(null, "REGISTRO_FALLIDO", false,
                    "Nombre duplicado: " + dto.getNombre());
            return "El nombre ya está en uso";
        }

        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setEmail(dto.getEmail());
        u.setPassword(encoder.encode(dto.getPassword()));
        u.setIntentosFallidos(0);
        u.setActivo(true);

        Rol rolUsuario = rolRepo.findByNombre("USUARIO");
        u.setRol(rolUsuario);

        usuarioRepo.save(u);

        registrarAuditoria(u, "REGISTRO_EXITOSO", true,
                "Usuario registrado correctamente");

        return "Usuario registrado correctamente";
    }


    // ============================================================
    // LOGIN (RF-02 + RNF-12 + RNF-02 + RNF-03 + RNF-13)
    // ============================================================
    public String login(LoginDTO dto, HttpSession session) {

        Usuario u = usuarioRepo.findByEmail(dto.getEmail());

        if (u == null) {
            registrarAuditoria(null, "LOGIN_FALLIDO", false,
                    "Email no encontrado: " + dto.getEmail());
            return "Usuario no encontrado";
        }

        if (!u.isActivo()) {
            registrarAuditoria(u, "LOGIN_BLOQUEADO", false,
                    "Usuario desactivado por intentos fallidos");
            return "Usuario bloqueado";
        }

        if (u.getIntentosFallidos() >= 5) {
            u.setActivo(false);
            usuarioRepo.save(u);

            registrarAuditoria(u, "LOGIN_BLOQUEADO", false,
                    "Usuario bloqueado automáticamente por 5 intentos fallidos");
            return "Usuario bloqueado por demasiados intentos";
        }

        boolean ok = encoder.matches(dto.getPassword(), u.getPassword());

        if (!ok) {
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            usuarioRepo.save(u);

            registrarAuditoria(u, "LOGIN_FALLIDO", false,
                    "Contraseña incorrecta. Intentos: " + u.getIntentosFallidos());

            return "Contraseña incorrecta";
        }

        // Login correcto
        u.setIntentosFallidos(0);
        usuarioRepo.save(u);

        session.setAttribute("usuarioId", u.getId());
        session.setAttribute("rol", u.getRol().getNombre());

        registrarAuditoria(u, "LOGIN_EXITOSO", true,
                "Inicio de sesión exitoso");

        return "Login correcto";
    }


    // ============================================================
    // LOGOUT (RF-21 + AUDITORÍA)
    // ============================================================
    public void logout(HttpSession session) {

        Long id = (Long) session.getAttribute("usuarioId");

        if (id != null) {
            usuarioRepo.findById(id).ifPresent(u ->
                    registrarAuditoria(u, "LOGOUT", true, "Cierre de sesión exitoso"));
        }

        session.invalidate();
    }


    // ============================================================
    // BLOQUEO POR ADMIN (MÓDULO C FUTURO) + AUDITORÍA
    // ============================================================
    public ResponseEntity<?> bloquear(Long adminId, Long usuarioId) {

        Optional<Usuario> optAdmin = usuarioRepo.findById(adminId);
        Optional<Usuario> optU = usuarioRepo.findById(usuarioId);

        if (optAdmin.isEmpty())
            return ResponseEntity.status(404).body("Admin no encontrado");

        Usuario admin = optAdmin.get();
        if (!"ADMIN".equalsIgnoreCase(admin.getRol().getNombre()))
            return ResponseEntity.status(403).body("No tienes permisos");

        if (optU.isEmpty())
            return ResponseEntity.status(404).body("Usuario no encontrado");

        Usuario u = optU.get();
        u.setActivo(false);
        usuarioRepo.save(u);

        registrarAuditoria(u, "BLOQUEADO_ADMIN", true,
                "Usuario bloqueado por administrador " + admin.getNombre());

        return ResponseEntity.ok("Usuario bloqueado");
    }

    public Usuario buscarPorIdentificador(String identificador) {
        if (identificador.contains("@")) {
            return usuarioRepo.findByCorreo(identificador)
                    .orElse(null);
        } else {
            return usuarioRepo.findByNombreUsuario(identificador)
                    .orElse(null);
        }
    }


}
