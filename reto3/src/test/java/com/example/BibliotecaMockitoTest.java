package com.example;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.example.exception.BibliotecaException;
import com.example.modelo.Libro;
import com.example.modelo.Prestamo;
import com.example.modelo.Usuario;
import com.example.repositorio.BookRepository;
import com.example.repositorio.LoanRepository;
import com.example.servicio.BibliotecaServicio;

class BibliotecaMockitoTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    private BibliotecaServicio bibliotecaServicio;

    private static final String ISBN = "1234567890";
    private static final String USUARIO_ID = "U001";
    private static final String TITULO = "Test Book";
    private static final String AUTOR = "Test Author";
    private static final String CATEGORIA = "Test Category";
    private static final int EJEMPLARES = 5;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bibliotecaServicio = new BibliotecaServicio(bookRepository, loanRepository);
    }

    @Test
    void agregarLibro_debeAgregarLibroCorrectamente() {
        // Arrange
        Libro libro = new Libro(ISBN, TITULO, AUTOR, CATEGORIA, EJEMPLARES);
        when(bookRepository.existsById(ISBN)).thenReturn(false);
        when(bookRepository.save(any(Libro.class))).thenReturn(libro);

        // Act
        Libro resultado = bibliotecaServicio.agregarLibro(libro);

        // Assert
        assertNotNull(resultado);
        assertEquals(ISBN, resultado.getIsbn());
        verify(bookRepository).save(libro);
    }

    @Test
    void buscarLibroPorId_debeRetornarLibroCorrectamente() {
        // Arrange
        Libro libro = new Libro(ISBN, TITULO, AUTOR, CATEGORIA, EJEMPLARES);
        when(bookRepository.findById(ISBN)).thenReturn(Optional.of(libro));

        // Act
        Libro resultado = bibliotecaServicio.obtenerLibroPorId(ISBN);

        // Assert
        assertNotNull(resultado);
        assertEquals(ISBN, resultado.getIsbn());
        assertEquals(TITULO, resultado.getTitulo());
    }

    @Test
    void crearUsuario_debeCrearUsuarioCorrectamente() {
        // Arrange
        Usuario usuario = new Usuario(USUARIO_ID, "Test User");

        // Act
        bibliotecaServicio.crearUsuario(usuario);

        // Assert
        // Verificar que el usuario se agregó a la lista de usuarios
        assertTrue(bibliotecaServicio.obtenerUsuarioPorId(USUARIO_ID) != null);
    }

    @Test
    void prestarLibro_debePrestarLibroCorrectamente() {
        // Arrange
        Libro libro = new Libro(ISBN, TITULO, AUTOR, CATEGORIA, EJEMPLARES);
        Usuario usuario = new Usuario(USUARIO_ID, "Test User");
        Prestamo prestamo = new Prestamo("P001", ISBN, USUARIO_ID);

        when(bookRepository.findById(ISBN)).thenReturn(Optional.of(libro));
        when(loanRepository.save(any(Prestamo.class))).thenReturn(prestamo);

        // Act
        bibliotecaServicio.crearUsuario(usuario);
        bibliotecaServicio.prestarLibro(ISBN, USUARIO_ID);

        // Assert
        verify(loanRepository).save(any(Prestamo.class));
        verify(bookRepository).save(any(Libro.class));
    }

    @Test
    void verPrestamosPorUsuario_debeRetornarPrestamosCorrectamente() {
        // Arrange
        Prestamo prestamo1 = new Prestamo("P001", ISBN, USUARIO_ID);
        Prestamo prestamo2 = new Prestamo("P002", "9876543210", USUARIO_ID);
        List<Prestamo> prestamos = Arrays.asList(prestamo1, prestamo2);

        when(loanRepository.findByUserId(USUARIO_ID)).thenReturn(prestamos);

        // Act
        List<Prestamo> resultado = bibliotecaServicio.obtenerPrestamosPorUsuario(USUARIO_ID);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(loanRepository).findByUserId(USUARIO_ID);
    }

    @Test
    void agregarLibro_conIsbnExistente_debeLanzarExcepcion() {
        // Arrange
        Libro libro = new Libro(ISBN, TITULO, AUTOR, CATEGORIA, EJEMPLARES);
        when(bookRepository.existsById(ISBN)).thenReturn(true);

        // Act & Assert
        assertThrows(BibliotecaException.class, () -> {
            bibliotecaServicio.agregarLibro(libro);
        });
    }

    @Test
    void prestarLibro_inexistente_debeLanzarExcepcion() {
        // Arrange
        when(bookRepository.findById(ISBN)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BibliotecaException.class, () -> {
            bibliotecaServicio.prestarLibro(ISBN, USUARIO_ID);
        });
    }

    @Test
    void devolverLibro_debeDevolverLibroCorrectamente() {
        // Arrange
        Libro libro = new Libro(ISBN, TITULO, AUTOR, CATEGORIA, EJEMPLARES);
        Prestamo prestamo = new Prestamo("P001", ISBN, USUARIO_ID);

        when(bookRepository.findById(ISBN)).thenReturn(Optional.of(libro));
        when(loanRepository.findActiveByUserId(USUARIO_ID))
            .thenReturn(Arrays.asList(prestamo));

        // Act
        bibliotecaServicio.devolverLibro(ISBN, USUARIO_ID);

        // Assert
        verify(loanRepository).update(any(Prestamo.class));
        verify(bookRepository).save(any(Libro.class));
    }
} 