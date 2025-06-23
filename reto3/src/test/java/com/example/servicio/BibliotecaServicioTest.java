package com.example.servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.exception.BibliotecaException;
import com.example.modelo.Libro;
import com.example.modelo.Prestamo;
import com.example.modelo.Usuario;
import com.example.repositorio.BookRepository;
import com.example.repositorio.LoanRepository;

@ExtendWith(MockitoExtension.class)
class BibliotecaServicioTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    private BibliotecaServicio bibliotecaServicio;

    @BeforeEach
    void setUp() {
        bibliotecaServicio = new BibliotecaServicio(bookRepository, loanRepository);
    }

    // Pruebas de Gestión de Libros
    @Test
    void testAgregarLibro() {
        // Arrange
        Libro libro = new Libro("123", "El Quijote", "Miguel de Cervantes", "Novela", 5);
        when(bookRepository.existsById("123")).thenReturn(false);
        when(bookRepository.save(any(Libro.class))).thenReturn(libro);

        // Act
        Libro resultado = bibliotecaServicio.agregarLibro(libro);

        // Assert
        assertNotNull(resultado);
        assertEquals("123", resultado.getIsbn());
        assertEquals("El Quijote", resultado.getTitulo());
        assertEquals("Miguel de Cervantes", resultado.getAutor());
        verify(bookRepository).save(libro);
    }

    @Test
    void testAgregarLibroDuplicado() {
        // Arrange
        Libro libro = new Libro("123", "El Quijote", "Miguel de Cervantes", "Novela", 5);
        when(bookRepository.existsById("123")).thenReturn(true);

        // Act & Assert
        assertThrows(BibliotecaException.class, () -> bibliotecaServicio.agregarLibro(libro));
    }

    @Test
    void testObtenerLibroPorId() {
        // Arrange
        Libro libro = new Libro("123", "El Quijote", "Miguel de Cervantes", "Novela", 5);
        when(bookRepository.findById("123")).thenReturn(Optional.of(libro));

        // Act
        Libro resultado = bibliotecaServicio.obtenerLibroPorId("123");

        // Assert
        assertNotNull(resultado);
        assertEquals("123", resultado.getIsbn());
        assertEquals("El Quijote", resultado.getTitulo());
    }

    @Test
    void testObtenerLibroPorIdNoExiste() {
        // Arrange
        when(bookRepository.findById("123")).thenReturn(Optional.empty());

        // Act & Assert
        BibliotecaException exception = assertThrows(BibliotecaException.class, 
            () -> bibliotecaServicio.obtenerLibroPorId("123"));
        assertTrue(exception.getMessage().contains("No se encontró el libro"));
    }

    // Pruebas de Gestión de Usuarios
    @Test
    void testCrearUsuario() {
        // Arrange
        Usuario usuario = new Usuario("U1", "Juan Pérez");

        // Act
        bibliotecaServicio.crearUsuario(usuario);

        // Assert
        Usuario resultado = bibliotecaServicio.obtenerUsuarioPorId("U1");
        assertNotNull(resultado);
        assertEquals("U1", resultado.getId());
        assertEquals("Juan Pérez", resultado.getNombre());
    }

    @Test
    void testCrearUsuarioDuplicado() {
        // Arrange
        Usuario usuario = new Usuario("U1", "Juan Pérez");
        bibliotecaServicio.crearUsuario(usuario);

        // Act & Assert
        assertThrows(BibliotecaException.class, () -> bibliotecaServicio.crearUsuario(usuario));
    }

    // Pruebas de Gestión de Préstamos
    @Test
    void testPrestarLibro() {
        // Arrange
        Libro libro = new Libro("123", "El Quijote", "Miguel de Cervantes", "Novela", 5);
        Usuario usuario = new Usuario("U1", "Juan Pérez");
        bibliotecaServicio.crearUsuario(usuario);

        when(bookRepository.findById("123")).thenReturn(Optional.of(libro));
        when(loanRepository.existsActiveLoan("123")).thenReturn(false);
        when(loanRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        bibliotecaServicio.prestarLibro("123", "U1");

        // Assert
        verify(bookRepository).save(any(Libro.class));
        verify(loanRepository).save(any(Prestamo.class));
    }

    @Test
    void testPrestarLibroNoDisponible() {
        // Arrange
        Libro libro = new Libro("123", "El Quijote", "Miguel de Cervantes", "Novela", 0);
        Usuario usuario = new Usuario("U1", "Juan Pérez");
        bibliotecaServicio.crearUsuario(usuario);

        when(bookRepository.findById("123")).thenReturn(Optional.of(libro));
        when(loanRepository.existsActiveLoan("123")).thenReturn(false);

        // Act & Assert
        assertThrows(BibliotecaException.class, () -> bibliotecaServicio.prestarLibro("123", "U1"));
    }

    @Test
    void testObtenerPrestamosPorUsuario() {
        // Arrange
        Usuario usuario = new Usuario("U1", "Juan Pérez");
        bibliotecaServicio.crearUsuario(usuario);

        Prestamo prestamo1 = new Prestamo("P1", "123", "U1");
        Prestamo prestamo2 = new Prestamo("P2", "456", "U1");
        List<Prestamo> prestamos = Arrays.asList(prestamo1, prestamo2);

        when(loanRepository.findByUserId("U1")).thenReturn(prestamos);

        // Act
        List<Prestamo> resultado = bibliotecaServicio.obtenerPrestamosPorUsuario("U1");

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("P1", resultado.get(0).getId());
        assertEquals("P2", resultado.get(1).getId());
    }

    @Test
    void testObtenerPrestamosPorUsuarioNoExiste() {
        // Act & Assert
        assertThrows(BibliotecaException.class, () -> bibliotecaServicio.obtenerPrestamosPorUsuario("U1"));
    }

    @Test
    void testPrestarLibroNoExiste() {
        // Arrange
        when(bookRepository.findById("123")).thenReturn(Optional.empty());

        // Act & Assert
        BibliotecaException exception = assertThrows(BibliotecaException.class, 
            () -> bibliotecaServicio.prestarLibro("123", "U1"));
        assertTrue(exception.getMessage().contains("No se encontró el libro"));
    }

    @Test
    void testPrestarLibroUsuarioNoExiste() {
        // Arrange
        Libro libro = new Libro("123", "El Quijote", "Miguel de Cervantes", "Novela", 5);
        when(bookRepository.findById("123")).thenReturn(Optional.of(libro));

        // Act & Assert
        BibliotecaException exception = assertThrows(BibliotecaException.class, 
            () -> bibliotecaServicio.prestarLibro("123", "U1"));
        assertTrue(exception.getMessage().contains("No se encontró el usuario"));
    }

    @Test
    void testPrestarLibroYaPrestado() {
        // Arrange
        Libro libro = new Libro("123", "El Quijote", "Miguel de Cervantes", "Novela", 5);
        Usuario usuario = new Usuario("U1", "Juan Pérez");
        bibliotecaServicio.crearUsuario(usuario);

        when(bookRepository.findById("123")).thenReturn(Optional.of(libro));
        when(loanRepository.existsActiveLoan("123")).thenReturn(true);

        // Act & Assert
        BibliotecaException exception = assertThrows(BibliotecaException.class, 
            () -> bibliotecaServicio.prestarLibro("123", "U1"));
        assertTrue(exception.getMessage().contains("El libro ya está prestado"));
    }

    @Test
    void testDevolverLibroNoPrestado() {
        // Arrange
        Libro libro = new Libro("123", "El Quijote", "Miguel de Cervantes", "Novela", 5);
        Usuario usuario = new Usuario("U1", "Juan Pérez");
        bibliotecaServicio.crearUsuario(usuario);

        when(bookRepository.findById("123")).thenReturn(Optional.of(libro));
        when(loanRepository.findActiveByUserId("U1")).thenReturn(List.of());

        // Act & Assert
        BibliotecaException exception = assertThrows(BibliotecaException.class, 
            () -> bibliotecaServicio.devolverLibro("123", "U1"));
        assertTrue(exception.getMessage().contains("No se encontró un préstamo activo"));
    }
} 