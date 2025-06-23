package com.example;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.modelo.Libro;
import com.example.modelo.Prestamo;
import com.example.modelo.Usuario;
import com.example.repositorio.BookRepository;
import com.example.repositorio.LoanRepository;
import com.example.servicio.BibliotecaServicio;

class BibliotecaPerformanceTest {
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
    void testRendimientoAgregarLibros() {
        int numLibros = 1000;
        List<Libro> libros = new ArrayList<>();
        
        // Preparar datos de prueba
        for (int i = 0; i < numLibros; i++) {
            libros.add(new Libro("L" + i, "Título " + i, "Autor " + i, "Categoría", 1));
        }
        
        // Medir tiempo de ejecución
        long startTime = System.nanoTime();
        
        for (Libro libro : libros) {
            when(bookRepository.save(libro)).thenReturn(libro);
            biblioteca.agregarLibro(libro);
        }
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        // Verificar rendimiento
        assertTrue(duration < 5000, "La operación tomó más de 5 segundos: " + duration + "ms");
        verify(bookRepository, times(numLibros)).save(any(Libro.class));
    }
    
    @Test
    void testRendimientoBusquedaLibros() {
        int numLibros = 1000;
        List<Libro> libros = new ArrayList<>();
        
        // Preparar datos de prueba
        for (int i = 0; i < numLibros; i++) {
            libros.add(new Libro("L" + i, "Título " + i, "Autor " + i, "Categoría", 1));
        }
        when(bookRepository.findAll()).thenReturn(libros);
        
        // Medir tiempo de búsqueda por título
        long startTime = System.nanoTime();
        List<Libro> resultados = biblioteca.buscarLibrosPorTitulo("Título");
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        // Verificar rendimiento
        assertTrue(duration < 1000, "La búsqueda tomó más de 1 segundo: " + duration + "ms");
        assertEquals(numLibros, resultados.size());
    }
    
    @Test
    void testRendimientoPrestamos() {
        int numUsuarios = 100;
        int numLibros = 100;
        List<Usuario> usuarios = new ArrayList<>();
        List<Libro> libros = new ArrayList<>();
        
        // Preparar datos de prueba
        for (int i = 0; i < numUsuarios; i++) {
            usuarios.add(new Usuario("U" + i, "Usuario " + i));
        }
        
        for (int i = 0; i < numLibros; i++) {
            libros.add(new Libro("L" + i, "Título " + i, "Autor " + i, "Categoría", 1));
        }
        
        // Configurar mocks
        for (Libro libro : libros) {
            when(bookRepository.findById(libro.getIsbn())).thenReturn(java.util.Optional.of(libro));
        }
        when(loanRepository.findActiveByUserId(anyString())).thenReturn(List.of());
        
        // Medir tiempo de ejecución de préstamos
        long startTime = System.nanoTime();
        
        for (int i = 0; i < numUsuarios; i++) {
            Usuario usuario = usuarios.get(i);
            biblioteca.crearUsuario(usuario);
            
            for (int j = 0; j < 3; j++) { // Máximo de préstamos por usuario
                Libro libro = libros.get((i + j) % numLibros);
                biblioteca.prestarLibro(libro.getIsbn(), usuario.getId());
            }
        }
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        // Verificar rendimiento
        assertTrue(duration < 10000, "Las operaciones de préstamo tomaron más de 10 segundos: " + duration + "ms");
        verify(loanRepository, times(numUsuarios * 3)).save(any(Prestamo.class));
    }
    
    @Test
    void testRendimientoDevoluciones() {
        int numPrestamos = 100;
        List<Prestamo> prestamos = new ArrayList<>();
        
        // Preparar datos de prueba
        for (int i = 0; i < numPrestamos; i++) {
            String libroId = "L" + i;
            String usuarioId = "U" + i;
            
            Libro libro = new Libro(libroId, "Título " + i, "Autor " + i, "Categoría", 1);
            Usuario usuario = new Usuario(usuarioId, "Usuario " + i);
            Prestamo prestamo = new Prestamo("P" + i, libroId, usuarioId, 
                LocalDateTime.now(), LocalDateTime.now().plusDays(15));
            
            when(bookRepository.findById(libroId)).thenReturn(java.util.Optional.of(libro));
            when(loanRepository.findActiveByUserId(usuarioId)).thenReturn(List.of(prestamo));
            
            biblioteca.crearUsuario(usuario);
            prestamos.add(prestamo);
        }
        
        // Medir tiempo de ejecución de devoluciones
        long startTime = System.nanoTime();
        
        for (Prestamo prestamo : prestamos) {
            biblioteca.devolverLibro(prestamo.getLibroIsbn(), prestamo.getUsuarioId());
        }
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        // Verificar rendimiento
        assertTrue(duration < 5000, "Las operaciones de devolución tomaron más de 5 segundos: " + duration + "ms");
        verify(loanRepository, times(numPrestamos)).save(any(Prestamo.class));
    }
} 