package com.example.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

public class Prestamo {
    private String id;
    private String libroIsbn;
    private String usuarioId;
    private LocalDateTime fechaPrestamo;
    private LocalDateTime fechaDevolucion;

    public Prestamo(String id, String libroIsbn, String usuarioId) {
        this(id, libroIsbn, usuarioId, LocalDateTime.now(), null);
    }

    public Prestamo(String id, String libroIsbn, String usuarioId, LocalDateTime fechaPrestamo, LocalDateTime fechaDevolucion) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del préstamo no puede estar vacío");
        }
        if (libroIsbn == null || libroIsbn.trim().isEmpty()) {
            throw new IllegalArgumentException("El ISBN del libro no puede estar vacío");
        }
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario no puede estar vacío");
        }
        if (fechaPrestamo == null) {
            throw new IllegalArgumentException("La fecha de préstamo no puede ser nula");
        }
        
        this.id = id;
        this.libroIsbn = libroIsbn;
        this.usuarioId = usuarioId;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getId() {
        return id;
    }

    public String getLibroIsbn() {
        return libroIsbn;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public boolean isActivo() {
        return fechaDevolucion == null;
    }

    public boolean estaVencido() {
        return isActivo() && LocalDateTime.now().isAfter(fechaPrestamo.plusDays(15));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prestamo prestamo = (Prestamo) o;
        return Objects.equals(id, prestamo.id) &&
               Objects.equals(libroIsbn, prestamo.libroIsbn) &&
               Objects.equals(usuarioId, prestamo.usuarioId) &&
               Objects.equals(fechaPrestamo, prestamo.fechaPrestamo) &&
               Objects.equals(fechaDevolucion, prestamo.fechaDevolucion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, libroIsbn, usuarioId, fechaPrestamo, fechaDevolucion);
    }

    @Override
    public String toString() {
        return "Prestamo{" +
                "id='" + id + '\'' +
                ", libroIsbn='" + libroIsbn + '\'' +
                ", usuarioId='" + usuarioId + '\'' +
                ", fechaPrestamo=" + fechaPrestamo +
                ", fechaDevolucion=" + fechaDevolucion +
                ", activo=" + isActivo() +
                ", vencido=" + estaVencido() +
                '}';
    }
} 