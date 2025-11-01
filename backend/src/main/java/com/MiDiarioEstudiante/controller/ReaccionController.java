package com.MiDiarioEstudiante.controller;

import com.MiDiarioEstudiante.dto.ReaccionRequest;
import com.MiDiarioEstudiante.model.Reaccion;
import com.MiDiarioEstudiante.model.enums.TipoReaccion;
import com.MiDiarioEstudiante.service.ReaccionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reacciones")
public class ReaccionController {

    private final ReaccionService reaccionService;

    public ReaccionController(ReaccionService reaccionService) {
        this.reaccionService = reaccionService;
    }

    @GetMapping("/publicacion/{publicacionId}")
    public List<Reaccion> listarPorPublicacion(@PathVariable Long publicacionId) {
        return reaccionService.obtenerPorPublicacion(publicacionId);
    }

    @GetMapping("/publicacion/{publicacionId}/resumen")
    public Map<TipoReaccion, Long> resumenPorPublicacion(@PathVariable Long publicacionId) {
        Map<TipoReaccion, Long> resumen = new EnumMap<>(TipoReaccion.class);
        for (TipoReaccion tipo : TipoReaccion.values()) {
            resumen.put(tipo, reaccionService.contarPorTipo(publicacionId, tipo));
        }
        return resumen;
    }

    @PostMapping
    public ResponseEntity<Reaccion> guardar(@RequestBody ReaccionRequest request) {
        Reaccion reaccion = new Reaccion();
        reaccion.setPublicacionId(request.getPublicacionId());
        reaccion.setUsuarioId(request.getUsuarioId());
        reaccion.setTipo(request.getTipo());
        Reaccion guardada = reaccionService.guardar(reaccion);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestParam Long publicacionId, @RequestParam Long usuarioId) {
        reaccionService.eliminar(publicacionId, usuarioId);
    }
}
