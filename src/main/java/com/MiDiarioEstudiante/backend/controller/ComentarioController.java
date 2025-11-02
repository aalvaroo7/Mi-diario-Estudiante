package com.MiDiarioEstudiante.backend.controller;

import com.MiDiarioEstudiante.backend.dto.ComentarioRequest;
import com.MiDiarioEstudiante.backend.model.Comentario;
import com.MiDiarioEstudiante.backend.service.ComentarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping("/publicacion/{publicacionId}")
    public List<Comentario> listarPorPublicacion(@PathVariable Long publicacionId) {
        return comentarioService.obtenerPorPublicacion(publicacionId);
    }

    @PostMapping
    public ResponseEntity<Comentario> crearComentario(@RequestBody ComentarioRequest request) {
        Comentario comentario = new Comentario();
        comentario.setPublicacionId(request.getPublicacionId());
        comentario.setUsuarioId(request.getUsuarioId());
        comentario.setContenido(request.getContenido());
        Comentario creado = comentarioService.crear(comentario);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @RequestParam Long usuarioId) {
        comentarioService.eliminar(id, usuarioId);
    }
}
