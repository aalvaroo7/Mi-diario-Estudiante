package com.miDiario.blog.controller;

import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public String registrar(@RequestBody Usuario usuario) {
        return usuarioService.registrarUsuario(usuario);
    }

    @PostMapping("/login")
    public Optional<Usuario> login(@RequestParam String nombreUsuario, @RequestParam String password) {
        return usuarioService.login(nombreUsuario, password);
    }
}
