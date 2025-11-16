package com.miDiario.blog.service;

import com.miDiario.blog.model.Publicacion;
import com.miDiario.blog.model.Usuario;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PublicacionService {
    private final DataStore dataStore;

    public PublicacionService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Optional<Publicacion> crear(String contenido, Usuario autor) {
        if (autor == null || contenido == null || contenido.isBlank()) {
            return Optional.empty();
        }
        Publicacion nueva = new Publicacion(dataStore.nextPostId(), contenido.trim(), autor);
        dataStore.getPublicaciones().add(nueva);
        return Optional.of(nueva);
    }

    public List<Publicacion> todasOrdenadas() {
        return dataStore.getPublicaciones().stream()
                .sorted(Comparator.comparing(Publicacion::getFechaPublicacion).reversed())
                .toList();
    }

    public boolean eliminar(long id, Usuario solicitante) {
        if (solicitante == null) return false;
        return dataStore.getPublicaciones().removeIf(p -> p.getId() == id && p.getAutor().equals(solicitante));
    }
}
