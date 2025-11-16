package com.miDiario.blog.controller;

import com.miDiario.blog.dto.AuthResponse;
import com.miDiario.blog.dto.LoginRequest;
import com.miDiario.blog.dto.RegistroRequest;
import com.miDiario.blog.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest solicitud) {
        return ResponseEntity.ok(usuarioService.registrarUsuario(solicitud));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest solicitud, HttpSession session) {
        return ResponseEntity.ok(usuarioService.login(solicitud, session));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        usuarioService.logout(session);
        return ResponseEntity.noContent().build();
    }
}
