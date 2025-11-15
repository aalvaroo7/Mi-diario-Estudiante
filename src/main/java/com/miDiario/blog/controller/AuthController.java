package com.miDiario.blog.controller;

import com.miDiario.blog.dto.RegistroDTO;
import com.miDiario.blog.dto.LoginDTO;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // REGISTRO
    @PostMapping("/registro")
    public String registrar(@RequestBody RegistroDTO dto) {
        return usuarioService.registrar(dto);
    }

    // LOGIN
    @PostMapping("/login")
    public String login(@RequestBody LoginDTO dto, HttpSession session) {
        return usuarioService.login(dto, session);
    }

    // LOGOUT
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        usuarioService.logout(session);
        return "Sesión cerrada";
    }
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {

        Usuario usuario = usuarioService.buscarPorIdentificador(loginDTO.getIdentificador());

        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario o email incorrecto");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }

        return ResponseEntity.ok(usuario);
    }

}
