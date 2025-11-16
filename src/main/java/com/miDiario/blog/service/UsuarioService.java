package com.miDiario.blog.service;

import com.miDiario.blog.dto.AuthResponse;
import com.miDiario.blog.dto.LoginRequest;
import com.miDiario.blog.dto.PerfilResponse;
import com.miDiario.blog.dto.RegistroRequest;
import com.miDiario.blog.model.Auditoria;
import com.miDiario.blog.model.Rol;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.repository.AuditoriaRepository;
import com.miDiario.blog.repository.RolRepository;
import com.miDiario.blog.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final int LIMITE_INTENTOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse registrarUsuario(RegistroRequest solicitud) {
        validarDuplicados(solicitud.getNombreUsuario(), solicitud.getCorreo());

        Rol rolUsuario = rolRepository.findByNombre("USUARIO")
                .orElseGet(() -> rolRepository.save(crearRolPorDefecto()));

        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.getNombre());
        usuario.setApellidos(solicitud.getApellidos());
        usuario.setNombreUsuario(solicitud.getNombreUsuario());
        usuario.setGenero(solicitud.getGenero());
        usuario.setCorreo(solicitud.getCorreo());
        usuario.setPassword(passwordEncoder.encode(solicitud.getPassword()));
        usuario.setRol(rolUsuario);
        usuarioRepository.save(usuario);

        registrarAuditoria("REGISTRO_USUARIO", "Usuario registrado con éxito", usuario, true);
        return new AuthResponse(usuario.getId(), usuario.getNombreUsuario(), usuario.getRol().getNombre(), "Usuario registrado correctamente.");
    }

    @Transactional
    public AuthResponse login(LoginRequest solicitud, HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(solicitud.getNombreUsuario());
        if (usuarioOpt.isEmpty()) {
            registrarAuditoria("LOGIN", "Intento de login fallido: usuario no encontrado", null, false);
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        Usuario usuario = usuarioOpt.get();
        LocalDateTime ahora = LocalDateTime.now();
        if (usuario.getCuentaBloqueadaHasta() != null && usuario.getCuentaBloqueadaHasta().isAfter(ahora)) {
            registrarAuditoria("LOGIN", "Cuenta bloqueada temporalmente", usuario, false);
            throw new IllegalStateException("La cuenta está bloqueada hasta " + usuario.getCuentaBloqueadaHasta());
        }

        if (!passwordEncoder.matches(solicitud.getPassword(), usuario.getPassword())) {
            manejarIntentoFallido(usuario);
            registrarAuditoria("LOGIN", "Contraseña incorrecta", usuario, false);
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        usuario.setIntentosFallidos(0);
        usuario.setCuentaBloqueadaHasta(null);
        usuarioRepository.save(usuario);

        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("nombreUsuario", usuario.getNombreUsuario());
        session.setAttribute("rol", usuario.getRol().getNombre());

        registrarAuditoria("LOGIN", "Inicio de sesión exitoso", usuario, true);
        return new AuthResponse(usuario.getId(), usuario.getNombreUsuario(), usuario.getRol().getNombre(), "Inicio de sesión exitoso");
    }

    @Transactional
    public void logout(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioRepository.findById(usuarioId).orElse(null);
        }
        registrarAuditoria("LOGOUT", "Cierre de sesión", usuario, true);
        session.invalidate();
    }

    @Transactional(readOnly = true)
    public PerfilResponse obtenerPerfil(Long id, HttpSession session) {
        Long usuarioSesion = (Long) session.getAttribute("usuarioId");
        if (usuarioSesion == null) {
            throw new SecurityException("Autenticación requerida");
        }
        if (!usuarioSesion.equals(id)) {
            throw new SecurityException("No tienes permiso para ver este perfil");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return new PerfilResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getNombreUsuario(),
                usuario.getGenero(),
                usuario.getCorreo(),
                usuario.getRol().getNombre()
        );
    }

    private void validarDuplicados(String nombreUsuario, String correo) {
        usuarioRepository.findByNombreUsuario(nombreUsuario).ifPresent(u -> {
            registrarAuditoria("REGISTRO_USUARIO", "Nombre de usuario duplicado", u, false);
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        });
        usuarioRepository.findByCorreo(correo).ifPresent(u -> {
            registrarAuditoria("REGISTRO_USUARIO", "Correo duplicado", u, false);
            throw new IllegalArgumentException("El correo ya está registrado");
        });
    }

    private Rol crearRolPorDefecto() {
        Rol rol = new Rol();
        rol.setNombre("USUARIO");
        rol.setDescripcion("Rol por defecto para nuevos usuarios");
        return rol;
    }

    private void manejarIntentoFallido(Usuario usuario) {
        int intentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(intentos);
        if (intentos >= LIMITE_INTENTOS) {
            usuario.setCuentaBloqueadaHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
        }
        usuarioRepository.save(usuario);
    }

    private void registrarAuditoria(String accion, String descripcion, Usuario usuario, boolean exito) {
        Auditoria auditoria = new Auditoria();
        auditoria.setAccion(accion);
        auditoria.setDescripcion(descripcion);
        auditoria.setExito(exito);
        auditoria.setFechaHora(LocalDateTime.now());
        auditoria.setUsuario(usuario);
        auditoriaRepository.save(auditoria);
    }
}
