package com.example.repositorio;

import java.util.List;
import java.util.Optional;

import com.example.modelo.Libro;

public interface BookRepository {
    Libro save(Libro libro);
    Optional<Libro> findById(String isbn);
    List<Libro> findAll();
    void delete(String isbn);
    boolean existsById(String isbn);
} 