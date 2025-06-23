package com.example.repositorio.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.example.exception.BibliotecaException;
import com.example.modelo.Prestamo;
import com.example.repositorio.LoanRepository;

public class LoanRepositoryImpl implements LoanRepository {
    private final ConcurrentMap<String, Prestamo> prestamos;

    public LoanRepositoryImpl() {
        this.prestamos = new ConcurrentHashMap<>();
    }

    @Override
    public Prestamo save(Prestamo prestamo) {
        if (prestamo == null) {
            throw new BibliotecaException("El préstamo no puede ser nulo");
        }
        if (prestamo.getId() == null || prestamo.getId().trim().isEmpty()) {
            throw new BibliotecaException("El ID del préstamo no puede estar vacío");
        }
        if (prestamo.getLibroIsbn() == null || prestamo.getLibroIsbn().trim().isEmpty()) {
            throw new BibliotecaException("El ISBN del libro no puede estar vacío");
        }
        if (prestamo.getUsuarioId() == null || prestamo.getUsuarioId().trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }
        prestamos.put(prestamo.getId(), prestamo);
        return prestamo;
    }

    @Override
    public Optional<Prestamo> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BibliotecaException("El ID del préstamo no puede estar vacío");
        }
        return Optional.ofNullable(prestamos.get(id));
    }

    @Override
    public List<Prestamo> findAll() {
        return new ArrayList<>(prestamos.values());
    }

    @Override
    public List<Prestamo> findByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }
        return prestamos.values().stream()
                .filter(p -> p.getUsuarioId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Prestamo> findActiveByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }
        return prestamos.values().stream()
                .filter(p -> p.getUsuarioId().equals(userId) && p.isActivo())
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsActiveLoan(String libroIsbn) {
        if (libroIsbn == null || libroIsbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN del libro no puede estar vacío");
        }
        return prestamos.values().stream()
                .anyMatch(p -> p.getLibroIsbn().equals(libroIsbn) && p.isActivo());
    }

    @Override
    public Prestamo update(Prestamo prestamo) {
        if (prestamo == null) {
            throw new BibliotecaException("El préstamo no puede ser nulo");
        }
        if (prestamo.getId() == null || prestamo.getId().trim().isEmpty()) {
            throw new BibliotecaException("El ID del préstamo no puede estar vacío");
        }
        if (!prestamos.containsKey(prestamo.getId())) {
            throw new BibliotecaException("No existe un préstamo con el ID: " + prestamo.getId());
        }
        prestamos.put(prestamo.getId(), prestamo);
        return prestamo;
    }

    @Override
    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BibliotecaException("El ID del préstamo no puede estar vacío");
        }
        if (!prestamos.containsKey(id)) {
            throw new BibliotecaException("No existe un préstamo con el ID: " + id);
        }
        prestamos.remove(id);
    }
} 