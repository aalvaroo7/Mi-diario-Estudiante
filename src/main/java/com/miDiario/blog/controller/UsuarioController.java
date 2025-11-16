package com.miDiario.blog.controller;

import com.miDiario.blog.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.miDiario.blog.dto.PerfilResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/perfil/{id}")
    public ResponseEntity<PerfilResponse> obtenerPerfil(@PathVariable Long id, HttpSession session) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(id, session));
    }
}
