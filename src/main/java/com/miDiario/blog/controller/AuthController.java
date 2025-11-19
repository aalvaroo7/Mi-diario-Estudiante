package com.miDiario.blog.controller;

import com.miDiario.blog.dto.LoginDTO;
import com.miDiario.blog.dto.RegistroDTO;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody RegistroDTO dto) {
        String r = usuarioService.registrar(dto);
        if (!r.equals("Usuario registrado correctamente"))
            return ResponseEntity.badRequest().body(r);
        return ResponseEntity.ok(r);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto, HttpSession session) {

        String resultado = usuarioService.login(dto, session);

        if (!resultado.equals("Login correcto"))
            return ResponseEntity.status(401).body(resultado);

        // Devolver usuario completo para localStorage
        Usuario u = usuarioService.buscarPorIdentificador(dto.getIdentificador());

        return ResponseEntity.ok(u);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        usuarioService.logout(session);
        return ResponseEntity.ok("Logout correcto");
    }
}
