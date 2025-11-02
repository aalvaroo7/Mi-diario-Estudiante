package com.MiDiarioEstudiante.backend.controller;

import com.MiDiarioEstudiante.backend.dto.PublicacionRequest;
import com.MiDiarioEstudiante.backend.model.Publicacion;
import com.MiDiarioEstudiante.backend.service.PublicacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionService publicacionService;

    public PublicacionController(PublicacionService publicacionService) {
        this.publicacionService = publicacionService;
    }

    @GetMapping
    public List<Publicacion> listarPublicaciones() {
        return publicacionService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public Publicacion obtenerPublicacion(@PathVariable Long id) {
        return publicacionService.buscarPorId(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Publicacion> listarPorUsuario(@PathVariable Long usuarioId) {
        return publicacionService.obtenerPorUsuario(usuarioId);
    }

    @PostMapping
    public ResponseEntity<Publicacion> crearPublicacion(@RequestBody PublicacionRequest request) {
        Publicacion publicacion = new Publicacion();
        publicacion.setUsuarioId(request.getUsuarioId());
        publicacion.setContenido(request.getContenido());
        publicacion.setVisibilidad(request.getVisibilidad());
        Publicacion creada = publicacionService.crear(publicacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @RequestParam Long usuarioId) {
        publicacionService.eliminar(id, usuarioId);
    }
}
