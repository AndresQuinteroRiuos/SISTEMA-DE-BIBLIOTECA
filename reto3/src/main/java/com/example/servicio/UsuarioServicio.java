package com.example.servicio;

import java.util.ArrayList;
import java.util.List;

import com.example.modelo.Usuario;

public class UsuarioServicio {
    private List<Usuario> usuarios;

    public UsuarioServicio() {
        this.usuarios = new ArrayList<>();
    }

    public void registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de usuario no puede estar vacío");
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }
        if (usuarios.stream().anyMatch(u -> u.getId().equals(usuario.getId()))) {
            throw new IllegalArgumentException("Ya existe un usuario con el ID: " + usuario.getId());
        }
        usuarios.add(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios);
    }

    public boolean existeUsuario(String usuarioId) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de usuario no puede estar vacío");
        }
        return usuarios.stream()
                .anyMatch(usuario -> usuario.getId().equals(usuarioId));
    }
} 