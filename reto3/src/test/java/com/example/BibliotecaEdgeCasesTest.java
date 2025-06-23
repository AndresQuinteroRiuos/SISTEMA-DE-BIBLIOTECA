package com.example;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.exception.BibliotecaException;
import com.example.modelo.Libro;
import com.example.modelo.Prestamo;
import com.example.modelo.Usuario;
import com.example.repositorio.BookRepository;
import com.example.repositorio.LoanRepository;
import com.example.servicio.BibliotecaServicio;

class BibliotecaEdgeCasesTest {
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private LoanRepository loanRepository;
    
    private BibliotecaServicio biblioteca;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        biblioteca = new BibliotecaServicio(bookRepository, loanRepository);
    }
    
    @Test
    void testAgregarLibroConDatosInvalidos() {
        // Test con libro nulo
        assertThrows(BibliotecaException.class, () -> biblioteca.agregarLibro(null));
        
        // Test con ISBN vacío
        Libro libroSinIsbn = new Libro("", "Título", "Autor", "Categoría", 1);
        assertThrows(BibliotecaException.class, () -> biblioteca.agregarLibro(libroSinIsbn));
        
        // Test con ejemplares negativos
        Libro libroEjemplaresNegativos = new Libro("123", "Título", "Autor", "Categoría", -1);
        assertThrows(BibliotecaException.class, () -> biblioteca.agregarLibro(libroEjemplaresNegativos));
    }
    
    @Test
    void testPrestarLibroConLimiteExcedido() {
        // Configurar usuario con máximo de préstamos
        String usuarioId = "U1";
        Usuario usuario = new Usuario(usuarioId, "Usuario Test");
        biblioteca.crearUsuario(usuario);
        
        // Configurar préstamos activos
        List<Prestamo> prestamosActivos = Arrays.asList(
            new Prestamo("P1", "L1", usuarioId, LocalDateTime.now(), LocalDateTime.now().plusDays(15)),
            new Prestamo("P2", "L2", usuarioId, LocalDateTime.now(), LocalDateTime.now().plusDays(15)),
            new Prestamo("P3", "L3", usuarioId, LocalDateTime.now(), LocalDateTime.now().plusDays(15))
        );
        when(loanRepository.findActiveByUserId(usuarioId)).thenReturn(prestamosActivos);
        
        // Intentar prestar un libro adicional
        Libro libro = new Libro("L4", "Título", "Autor", "Categoría", 1);
        when(bookRepository.findById("L4")).thenReturn(java.util.Optional.of(libro));
        
        assertThrows(BibliotecaException.class, () -> biblioteca.prestarLibro("L4", usuarioId));
    }
    
    @Test
    void testDevolverLibroNoPrestado() {
        String libroId = "L1";
        String usuarioId = "U1";
        
        // Configurar libro existente
        Libro libro = new Libro(libroId, "Título", "Autor", "Categoría", 1);
        when(bookRepository.findById(libroId)).thenReturn(java.util.Optional.of(libro));
        
        // Configurar que no hay préstamo activo
        when(loanRepository.findActiveByUserId(usuarioId)).thenReturn(List.of());
        
        assertThrows(BibliotecaException.class, () -> biblioteca.devolverLibro(libroId, usuarioId));
    }
    
    @Test
    void testBuscarLibroInexistente() {
        String libroId = "L1";
        when(bookRepository.findById(libroId)).thenReturn(java.util.Optional.empty());
        
        assertThrows(BibliotecaException.class, () -> biblioteca.obtenerLibroPorId(libroId));
    }
    
    @Test
    void testCrearUsuarioDuplicado() {
        String usuarioId = "U1";
        Usuario usuario1 = new Usuario(usuarioId, "Usuario 1");
        Usuario usuario2 = new Usuario(usuarioId, "Usuario 2");
        
        biblioteca.crearUsuario(usuario1);
        assertThrows(BibliotecaException.class, () -> biblioteca.crearUsuario(usuario2));
    }
    
    @Test
    void testPrestarLibroSinEjemplares() {
        String libroId = "L1";
        String usuarioId = "U1";
        
        // Configurar libro sin ejemplares disponibles
        Libro libro = new Libro(libroId, "Título", "Autor", "Categoría", 0);
        when(bookRepository.findById(libroId)).thenReturn(java.util.Optional.of(libro));
        
        // Configurar usuario existente
        Usuario usuario = new Usuario(usuarioId, "Usuario Test");
        biblioteca.crearUsuario(usuario);
        
        assertThrows(BibliotecaException.class, () -> biblioteca.prestarLibro(libroId, usuarioId));
    }
} 