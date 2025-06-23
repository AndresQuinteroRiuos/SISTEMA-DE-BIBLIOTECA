package com.example.repositorio;

import java.util.List;
import java.util.Optional;

import com.example.modelo.Prestamo;

public interface LoanRepository {
    Prestamo save(Prestamo prestamo);
    Optional<Prestamo> findById(String id);
    List<Prestamo> findAll();
    List<Prestamo> findByUserId(String userId);
    List<Prestamo> findActiveByUserId(String userId);
    boolean existsActiveLoan(String libroIsbn);
    Prestamo update(Prestamo prestamo);
    void delete(String id);
} 