package com.example.servicio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

import com.example.exception.BibliotecaException;
import com.example.modelo.Libro;
import com.example.modelo.Prestamo;
import com.example.modelo.Usuario;
import com.example.repositorio.BookRepository;
import com.example.repositorio.LoanRepository;

/**
 * Servicio principal para la gestión de la biblioteca.
 * Maneja las operaciones relacionadas con libros, usuarios y préstamos.
 */
public class BibliotecaServicio {
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final List<Usuario> usuarios;
    private static final int MAX_PRESTAMOS_POR_USUARIO = 3;
    private static final int DIAS_PRESTAMO = 15;

    public BibliotecaServicio(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.usuarios = new ArrayList<>();
    }

    /**
     * Agrega un nuevo libro al sistema.
     * @param libro El libro a agregar
     * @return El libro agregado
     * @throws BibliotecaException si el libro ya existe o los datos son inválidos
     */
    public Libro agregarLibro(Libro libro) {
        if (libro == null) {
            throw new BibliotecaException("El libro no puede ser nulo");
        }
        if (libro.getIsbn() == null || libro.getIsbn().trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        if (bookRepository.existsById(libro.getIsbn())) {
            throw new BibliotecaException("Ya existe un libro con el ISBN: " + libro.getIsbn());
        }
        return bookRepository.save(libro);
    }

    /**
     * Obtiene un libro por su ISBN.
     * @param isbn El ISBN del libro
     * @return El libro encontrado
     * @throws BibliotecaException si el libro no existe
     */
    public Libro obtenerLibroPorId(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        return bookRepository.findById(isbn)
                .orElseThrow(() -> new BibliotecaException("No se encontró el libro con ISBN: " + isbn));
    }

    /**
     * Busca libros por autor.
     * @param autor El nombre del autor
     * @return Lista de libros del autor
     */
    public List<Libro> buscarLibrosPorAutor(String autor) {
        if (autor == null || autor.trim().isEmpty()) {
            throw new BibliotecaException("El autor no puede estar vacío");
        }
        return bookRepository.findAll().stream()
                .filter(libro -> libro.getAutor().toLowerCase().contains(autor.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Lista todos los libros del sistema.
     * @return Lista de todos los libros
     */
    public List<Libro> listarLibros() {
        return new ArrayList<>(bookRepository.findAll());
    }

    /**
     * Busca libros por título.
     * @param titulo El título a buscar
     * @return Lista de libros que coinciden con el título
     */
    public List<Libro> buscarLibrosPorTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new BibliotecaException("El título no puede estar vacío");
        }
        return bookRepository.findAll().stream()
                .filter(libro -> libro.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Lista los libros disponibles para préstamo.
     * @return Lista de libros disponibles
     */
    public List<Libro> listarLibrosDisponibles() {
        return bookRepository.findAll().stream()
                .filter(libro -> libro.getEjemplaresDisponibles() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * @param usuario El usuario a crear
     * @throws BibliotecaException si el usuario ya existe o los datos son inválidos
     */
    public void crearUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new BibliotecaException("El usuario no puede ser nulo");
        }
        if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }
        if (usuarios.stream().anyMatch(u -> u.getId().equals(usuario.getId()))) {
            throw new BibliotecaException("Ya existe un usuario con el ID: " + usuario.getId());
        }
        usuarios.add(usuario);
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id El ID del usuario
     * @return El usuario encontrado
     * @throws BibliotecaException si el usuario no existe
     */
    public Usuario obtenerUsuarioPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }
        return usuarios.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BibliotecaException("No se encontró el usuario con ID: " + id));
    }

    /**
     * Busca usuarios por nombre.
     * @param nombre El nombre a buscar
     * @return Lista de usuarios que coinciden con el nombre
     */
    public List<Usuario> buscarUsuariosPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BibliotecaException("El nombre no puede estar vacío");
        }
        return usuarios.stream()
                .filter(usuario -> usuario.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Presta un libro a un usuario.
     * @param isbn El ISBN del libro
     * @param usuarioId El ID del usuario
     * @throws BibliotecaException si el libro no está disponible o el usuario tiene demasiados préstamos
     */
    public void prestarLibro(String isbn, String usuarioId) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }

        Libro libro = obtenerLibroPorId(isbn);
        Usuario usuario = obtenerUsuarioPorId(usuarioId);

        validarDisponibilidadLibro(libro);
        validarLibroNoPrestado(isbn, usuarioId);
        validarLimitePrestamos(usuarioId);

        Prestamo prestamo = new Prestamo(
            "P" + System.currentTimeMillis(),
            isbn,
            usuarioId,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(DIAS_PRESTAMO)
        );

        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() - 1);
        bookRepository.save(libro);
        loanRepository.save(prestamo);
    }

    private void validarDisponibilidadLibro(Libro libro) {
        if (libro.getEjemplaresDisponibles() <= 0) {
            throw new BibliotecaException("No hay ejemplares disponibles del libro");
        }
    }

    private void validarLibroNoPrestado(String isbn, String usuarioId) {
        if (tienePrestamoActivo(isbn, usuarioId)) {
            throw new BibliotecaException("El usuario ya tiene prestado este libro");
        }
    }

    private void validarLimitePrestamos(String usuarioId) {
        List<Prestamo> prestamosActivos = loanRepository.findActiveByUserId(usuarioId);
        if (prestamosActivos.size() >= MAX_PRESTAMOS_POR_USUARIO) {
            throw new BibliotecaException("El usuario ha alcanzado el límite de préstamos permitidos");
        }
    }

    /**
     * Devuelve un libro prestado.
     * @param isbn El ISBN del libro
     * @param usuarioId El ID del usuario
     * @throws BibliotecaException si el libro no está prestado por el usuario
     */
    public void devolverLibro(String isbn, String usuarioId) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }

        Libro libro = obtenerLibroPorId(isbn);
        Prestamo prestamo = obtenerPrestamoActivo(isbn, usuarioId);

        prestamo.setFechaDevolucion(LocalDateTime.now());
        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() + 1);

        bookRepository.save(libro);
        loanRepository.save(prestamo);
    }

    private Prestamo obtenerPrestamoActivo(String isbn, String usuarioId) {
        return loanRepository.findActiveByUserId(usuarioId).stream()
                .filter(p -> p.getLibroIsbn().equals(isbn))
                .findFirst()
                .orElseThrow(() -> new BibliotecaException("No se encontró un préstamo activo para este libro y usuario"));
    }

    /**
     * Obtiene todos los préstamos de un usuario.
     * @param usuarioId El ID del usuario
     * @return Lista de préstamos del usuario
     */
    public List<Prestamo> obtenerPrestamosPorUsuario(String usuarioId) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }
        return loanRepository.findByUserId(usuarioId);
    }

    /**
     * Obtiene los préstamos activos de un usuario.
     * @param usuarioId El ID del usuario
     * @return Lista de préstamos activos del usuario
     */
    public List<Prestamo> obtenerPrestamosActivosPorUsuario(String usuarioId) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }
        return loanRepository.findActiveByUserId(usuarioId);
    }

    /**
     * Lista todos los libros prestados actualmente.
     * @return Lista de libros prestados
     */
    public List<Libro> listarLibrosPrestados() {
        return loanRepository.findAll().stream()
                .filter(Prestamo::isActivo)
                .map(prestamo -> obtenerLibroPorId(prestamo.getLibroIsbn()))
                .collect(Collectors.toList());
    }

    /**
     * Elimina un libro del sistema.
     * @param isbn El ISBN del libro
     * @throws BibliotecaException si el libro tiene préstamos activos
     */
    public void eliminarLibro(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        if (loanRepository.existsActiveLoan(isbn)) {
            throw new BibliotecaException("No se puede eliminar un libro que tiene préstamos activos");
        }
        bookRepository.delete(isbn);
    }

    /**
     * Elimina el historial de un préstamo.
     * @param isbn El ISBN del libro
     * @param usuarioId El ID del usuario
     * @throws BibliotecaException si el préstamo no existe o está activo
     */
    public void borrarHistorialPrestamo(String isbn, String usuarioId) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }

        List<Prestamo> prestamos = loanRepository.findByUserId(usuarioId).stream()
                .filter(p -> p.getLibroIsbn().equals(isbn))
                .collect(Collectors.toList());

        for (Prestamo prestamo : prestamos) {
            loanRepository.delete(prestamo.getId());
        }
    }

    /**
     * Verifica si existe un libro con el ISBN dado.
     * @param isbn El ISBN a verificar
     * @return true si el libro existe, false en caso contrario
     */
    public boolean existeLibro(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        return bookRepository.existsById(isbn);
    }

    /**
     * Verifica si existe un usuario con el ID dado.
     * @param id El ID a verificar
     * @return true si el usuario existe, false en caso contrario
     */
    public boolean existeUsuario(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }
        return usuarios.stream().anyMatch(u -> u.getId().equals(id));
    }

    /**
     * Verifica si un usuario tiene un préstamo activo de un libro.
     * @param isbn El ISBN del libro
     * @param usuarioId El ID del usuario
     * @return true si el usuario tiene el libro prestado, false en caso contrario
     */
    public boolean tienePrestamoActivo(String isbn, String usuarioId) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BibliotecaException("El ISBN no puede estar vacío");
        }
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new BibliotecaException("El ID del usuario no puede estar vacío");
        }
        return loanRepository.findActiveByUserId(usuarioId).stream()
                .anyMatch(p -> p.getLibroIsbn().equals(isbn));
    }
} 