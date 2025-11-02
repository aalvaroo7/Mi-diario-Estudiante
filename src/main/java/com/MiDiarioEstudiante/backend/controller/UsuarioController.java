package com.MiDiarioEstudiante.backend.controller;

import com.MiDiarioEstudiante.backend.dto.LoginRequest;
import com.MiDiarioEstudiante.backend.dto.UsuarioResponse;
import com.MiDiarioEstudiante.backend.model.Usuario;
import com.MiDiarioEstudiante.backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.obtenerTodos().stream()
                .map(UsuarioResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtenerUsuario(@PathVariable Long id) {
        return UsuarioResponse.from(usuarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@RequestBody Usuario usuario) {
        Usuario nuevo = usuarioService.registrar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.from(nuevo));
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return UsuarioResponse.from(usuarioService.actualizar(id, usuario));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }

    @PostMapping("/login")
    public UsuarioResponse login(@RequestBody LoginRequest loginRequest) {
        Usuario usuario = usuarioService.autenticar(loginRequest.getEmail(), loginRequest.getPassword());
        return UsuarioResponse.from(usuario);
    }
}
