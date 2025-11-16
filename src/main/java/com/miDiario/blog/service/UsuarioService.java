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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    private final RolRepository rolRepo;
    private final UsuarioRepository usuarioRepo;
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
    // LOGIN FLEXIBLE (email o usuario)
    // ============================================================
    public Usuario buscarPorIdentificador(String identificador) {

        // si contiene @ → email
        if (identificador.contains("@")) {
            return usuarioRepo.findByEmail(identificador);
        }

        // si no → nombreUsuario
        return usuarioRepo.findByNombreUsuario(identificador);
    }

    // ============================================================
    // REGISTRO EXACTO según RegistroDTO y Usuario
    // ============================================================
    public String registrar(RegistroDTO dto) {

        if (usuarioRepo.existsByEmail(dto.getEmail())) {
            return "El correo ya está registrado";
        }

        if (usuarioRepo.existsByNombreUsuario(dto.getNombreUsuario())) {
            return "El nombre de usuario ya está en uso";
        }

        if (dto.getPassword().length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres";
        }

        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setNombreUsuario(dto.getNombreUsuario());
        u.setEmail(dto.getEmail());      // ✔ Este sí existe
        u.setPassword(encoder.encode(dto.getPassword()));
        Rol rolUsuario = rolRepo.findByNombre("USUARIO");
        if (rolUsuario == null) {
            throw new RuntimeException("ERROR: El rol 'USUARIO' no existe en la base de datos");
        }
        u.setRol(rolUsuario);
        u.setActivo(true);
        u.setIntentosFallidos(0);

        usuarioRepo.save(u);

        registrarAuditoria(u, "REGISTRO_USUARIO", true,
                "Usuario registrado correctamente");

        return "Usuario registrado correctamente";
    }


    // ============================================================
    // LOGIN
    // ============================================================
    public String login(LoginDTO dto, HttpSession session) {

        Usuario u = buscarPorIdentificador(dto.getIdentificador());

        if (u == null) {
            registrarAuditoria(null, "LOGIN_FALLIDO", false,
                    "Usuario/email no encontrado");
            return "Usuario no encontrado";
        }

        if (!u.isActivo()) {
            registrarAuditoria(u, "LOGIN_FALLIDO", false,
                    "Usuario bloqueado");
            return "Usuario bloqueado";
        }

        String passwordBD = u.getPassword();
        String passwordLogin = dto.getPassword();

        // Contraseñas antiguas sin hash
        if (!passwordBD.startsWith("$2a$")) {
            u.setPassword(encoder.encode(passwordBD));
            usuarioRepo.save(u);
        }

        if (!encoder.matches(passwordLogin, u.getPassword())) {

            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            usuarioRepo.save(u);

            registrarAuditoria(u, "LOGIN_FALLIDO", false,
                    "Contraseña incorrecta");

            return "Contraseña incorrecta";
        }

        // LOGIN OK
        u.setIntentosFallidos(0);
        usuarioRepo.save(u);

        session.setAttribute("usuarioId", u.getId());
        session.setAttribute("nombre", u.getNombre());
        session.setAttribute("rol", u.getRol());   // ✔ string

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
            usuarioRepo.findById(id)
                    .ifPresent(u -> registrarAuditoria(u, "LOGOUT", true,
                            "Cierre de sesión"));
        }

        session.invalidate();
    }

    // ============================================================
    // AUDITORÍA
    // ============================================================
    private void registrarAuditoria(Usuario usuario, String accion,
                                    boolean exito, String detalles) {

        Auditoria audit = new Auditoria();
        audit.setUsuario(usuario);
        audit.setAccion(accion);
        audit.setExito(exito);
        audit.setDetalles(detalles);

        auditoriaRepo.save(audit);
    }
}
