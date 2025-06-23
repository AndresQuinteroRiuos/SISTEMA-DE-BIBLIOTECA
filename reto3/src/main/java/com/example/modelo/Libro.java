package com.example.modelo;

import java.util.Objects;

public class Libro {
    private String isbn;
    private String titulo;
    private String autor;
    private String categoria;
    private int ejemplaresDisponibles;

    public Libro(String isbn, String titulo, String autor, String categoria, int ejemplaresDisponibles) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("El ISBN no puede estar vacío");
        }
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("El autor no puede estar vacío");
        }
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede estar vacía");
        }
        if (ejemplaresDisponibles < 0) {
            throw new IllegalArgumentException("El número de ejemplares no puede ser negativo");
        }
        
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getEjemplaresDisponibles() {
        return ejemplaresDisponibles;
    }

    public void setEjemplaresDisponibles(int ejemplaresDisponibles) {
        if (ejemplaresDisponibles < 0) {
            throw new IllegalArgumentException("El número de ejemplares no puede ser negativo");
        }
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Libro libro = (Libro) o;
        return Objects.equals(isbn, libro.isbn) &&
               Objects.equals(titulo, libro.titulo) &&
               Objects.equals(autor, libro.autor) &&
               Objects.equals(categoria, libro.categoria) &&
               ejemplaresDisponibles == libro.ejemplaresDisponibles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn, titulo, autor, categoria, ejemplaresDisponibles);
    }

    @Override
    public String toString() {
        return "Libro{" +
                "isbn='" + isbn + '\'' +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", categoria='" + categoria + '\'' +
                ", ejemplaresDisponibles=" + ejemplaresDisponibles +
                '}';
    }
} 