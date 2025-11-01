package com.MiDiarioEstudiante.repository;

import com.MiDiarioEstudiante.model.Usuario;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UsuarioRepository {
    private final ConcurrentMap<Long, Usuario> data = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public List<Usuario> findAll() {
        return new ArrayList<>(data.values());
    }

    public Optional<Usuario> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    public Optional<Usuario> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return data.values().stream()
                .filter(usuario -> email.equalsIgnoreCase(usuario.getEmail()))
                .findFirst();
    }

    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setId(sequence.getAndIncrement());
        }
        data.put(usuario.getId(), usuario);
        return usuario;
    }

    public void deleteById(Long id) {
        data.remove(id);
    }
}
