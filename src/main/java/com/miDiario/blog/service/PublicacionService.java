package com.miDiario.blog.service;

import com.miDiario.blog.model.Publicacion;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.repository.PublicacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;

    public PublicacionService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    public Publicacion crear(Publicacion p) {
        p.setFechaPublicacion(LocalDateTime.now());
        return publicacionRepository.save(p);
    }

    public List<Publicacion> todas() {
        return publicacionRepository.findAll();
    }

    public List<Publicacion> porUsuario(Usuario u) {
        return publicacionRepository.findByAutor(u);
    }

    public Optional<Publicacion> porId(Long id) {
        return publicacionRepository.findById(id);
    }

    public void eliminar(Long id) {
        publicacionRepository.deleteById(id);
    }
}
