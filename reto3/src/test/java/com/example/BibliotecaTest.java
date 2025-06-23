package com.example;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.modelo.Libro;
import com.example.modelo.Usuario;
import com.example.servicio.BibliotecaServicio;
import com.example.servicio.UsuarioServicio;

public class BibliotecaTest {
    private static final String ISBN = "1234567890";
    private static final String USUARIO_ID = "U001";
   // private static final String EMAIL_VALIDO = "test@example.com";
   // private static final String EMAIL_INVALIDO = "invalid-email";
    private static final String TITULO = "Test Book";
    private static final String AUTOR = "Test Author";
    private static final String CATEGORIA = "Test Category";
    private static final int EJEMPLARES = 5;

    private BibliotecaServicio bibliotecaServicio;
    private UsuarioServicio usuarioServicio;

    // Add imports for the repositories if not already present
    private com.example.repositorio.BookRepository bookRepository;
    private com.example.repositorio.LoanRepository loanRepository;

    @BeforeEach
    void setUp() {
       // bookRepository = new com.example.repositorio.InMemoryBookRepository();
        // Use an in-memory implementation for LoanRepository
        //loanRepository = new com.example.repositorio.InMemoryLoanRepository();
        bibliotecaServicio = new BibliotecaServicio(bookRepository, loanRepository);
        usuarioServicio = new UsuarioServicio();
    }

    @Test
    void agregarLibro_debeAgregarLibroCorrectamente() {
        Libro libro = new Libro(ISBN, TITULO, AUTOR, CATEGORIA, EJEMPLARES);
        bibliotecaServicio.agregarLibro(libro);

        List<Libro> libros = bibliotecaServicio.listarLibros();
        assertFalse(libros.isEmpty());
        assertEquals(1, libros.size());
        assertEquals(ISBN, libros.get(0).getIsbn());
    }

    @Test
    void registrarUsuario_debeRegistrarUsuarioCorrectamente() {
        Usuario usuario = new Usuario(USUARIO_ID, "Test User");
        usuarioServicio.registrarUsuario(usuario);

        List<Usuario> usuarios = usuarioServicio.listarUsuarios();
        assertFalse(usuarios.isEmpty());
        assertEquals(1, usuarios.size());
        assertEquals(USUARIO_ID, usuarios.get(0).getId());
    }

    @Test
    void prestarLibro_debePrestarLibroCorrectamente() {
        Libro libro = new Libro(ISBN, TITULO, AUTOR, CATEGORIA, EJEMPLARES);
        Usuario usuario = new Usuario(USUARIO_ID, "Test User");

        bibliotecaServicio.agregarLibro(libro);
        usuarioServicio.registrarUsuario(usuario);

        bibliotecaServicio.prestarLibro(ISBN, USUARIO_ID);

        List<Libro> librosPrestados = bibliotecaServicio.listarLibrosPrestados();
        assertFalse(librosPrestados.isEmpty());
        assertEquals(1, librosPrestados.size());
        assertEquals(ISBN, librosPrestados.get(0).getIsbn());
    }

    @Test
    void devolverLibro_debeDevolverLibroCorrectamente() {
        Libro libro = new Libro(ISBN, TITULO, AUTOR, CATEGORIA, EJEMPLARES);
        Usuario usuario = new Usuario(USUARIO_ID, "Test User");

        bibliotecaServicio.agregarLibro(libro);
        usuarioServicio.registrarUsuario(usuario);

        bibliotecaServicio.prestarLibro(ISBN, USUARIO_ID);
        bibliotecaServicio.devolverLibro(ISBN, USUARIO_ID);

        List<Libro> librosPrestados = bibliotecaServicio.listarLibrosPrestados();
        assertTrue(librosPrestados.isEmpty());
    }

    @Test
    void buscarLibrosPorTitulo_debeEncontrarLibrosPorTitulo() {
        Libro libro1 = new Libro(ISBN, "Java Programming", "Author 1", "Programming", EJEMPLARES);
        Libro libro2 = new Libro("0987654321", "Python Programming", "Author 2", "Programming", 3);

        bibliotecaServicio.agregarLibro(libro1);
        bibliotecaServicio.agregarLibro(libro2);

        List<Libro> resultados = bibliotecaServicio.buscarLibrosPorTitulo("Programming");
        assertEquals(2, resultados.size());
    }

    @Test
    void agregarLibro_conIsbnInvalido_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            Libro libro = new Libro("", TITULO, AUTOR, CATEGORIA, EJEMPLARES);
            bibliotecaServicio.agregarLibro(libro);
        });
    }

    @Test
    void registrarUsuario_conEmailInvalido_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            Usuario usuario = new Usuario(USUARIO_ID, "Test User");
            usuarioServicio.registrarUsuario(usuario);
        });
    }

    @Test
    void prestarLibro_inexistente_debeLanzarExcepcion() {
        assertThrows(NoSuchElementException.class, () -> {
            bibliotecaServicio.prestarLibro(ISBN, USUARIO_ID);
        });
    }

    @Test
    void devolverLibro_noPrestado_debeLanzarExcepcion() {
        Libro libro = new Libro(ISBN, TITULO, AUTOR, CATEGORIA, EJEMPLARES);
        bibliotecaServicio.agregarLibro(libro);
        assertThrows(IllegalStateException.class, () -> {
            bibliotecaServicio.devolverLibro(ISBN, USUARIO_ID);
        });
    }
}