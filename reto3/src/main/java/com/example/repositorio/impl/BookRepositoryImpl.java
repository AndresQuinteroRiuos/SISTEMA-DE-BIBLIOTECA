package com.example.repositorio.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.example.exception.BibliotecaException;
import com.example.modelo.Libro;
import com.example.repositorio.BookRepository;

public class BookRepositoryImpl implements BookRepository {
    private final ConcurrentMap<String, Libro> libros;

    public BookRepositoryImpl() {
        this.libros = new ConcurrentHashMap<>();
    }

    @Override
    public Libro save(Libro libro) {
        if (libro == null) {
            throw new BibliotecaException("El libro no puede ser nulo");
        }
        if (libro.getIsbn() == null || libro.getIsbn().trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        libros.put(libro.getIsbn(), libro);
        return libro;
    }

    @Override
    public Optional<Libro> findById(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        return Optional.ofNullable(libros.get(isbn));
    }

    @Override
    public List<Libro> findAll() {
        return new ArrayList<>(libros.values());
    }

    @Override
    public void delete(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        if (!libros.containsKey(isbn)) {
            throw new BibliotecaException("No existe un libro con el ISBN: " + isbn);
        }
        libros.remove(isbn);
    }

    @Override
    public boolean existsById(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        return libros.containsKey(isbn);
    }
} 