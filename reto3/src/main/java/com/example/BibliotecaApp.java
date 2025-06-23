package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

import com.example.exception.BibliotecaException;
import com.example.modelo.Libro;
import com.example.modelo.Prestamo;
import com.example.modelo.Usuario;
import com.example.repositorio.BookRepository;
import com.example.repositorio.LoanRepository;
import com.example.repositorio.impl.BookRepositoryImpl;
import com.example.repositorio.impl.LoanRepositoryImpl;
import com.example.servicio.BibliotecaServicio;

public class BibliotecaApp {
    private static final BibliotecaServicio biblioteca;
    private static final Scanner scanner;
    private static final MenuManager menuManager;

    static {
        BookRepository bookRepository = new BookRepositoryImpl();
        LoanRepository loanRepository = new LoanRepositoryImpl();
        biblioteca = new BibliotecaServicio(bookRepository, loanRepository);
        scanner = new Scanner(System.in);
        menuManager = new MenuManager();
    }

    public static void main(String[] args) {
        try {
            menuManager.iniciar();
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static class MenuManager {
        private static final String SEPARADOR = "\n=== %s ===";
        private static final String ERROR = "Error: %s";
        private static final String EXITO = "Operación exitosa: %s";

        public void iniciar() {
            Menu menuPrincipal = crearMenuPrincipal();
            menuPrincipal.mostrar();
        }

        private Menu crearMenuPrincipal() {
            return new Menu("SISTEMA DE GESTIÓN DE BIBLIOTECA")
                .agregarOpcion("Agregar Libro a la biblioteca ", this::agregarLibro)
                .agregarOpcion("Buscar Libro existente en la biblioteca", this::buscarLibroPorId)
                .agregarOpcion("Crear Usuario", this::crearUsuario)
                .agregarOpcion("Prestar Libro a un usuario", this::prestarLibro)
                .agregarOpcion("Devolver Libro a la biblioteca", this::devolverLibro)
                .agregarOpcion("Ver Inventario de libros disponibles", this::mostrarInventario)
                .agregarOpcion("Ver Préstamos de un usuario", this::verPrestamosPorUsuario)
                .agregarOpcion("Salir", () -> true);
        }

        private boolean agregarLibro() {
            try {
                System.out.printf(SEPARADOR, "AGREGAR LIBRO");
                String id = InputManager.leerTexto("ID del libro: ");
                String titulo = InputManager.leerTexto("Título: ");
                String autor = InputManager.leerTexto("Autor: ");

                Libro libro = new Libro(id, titulo, autor, "General", 3);
                biblioteca.agregarLibro(libro);
                System.out.printf(EXITO, "Libro agregado exitosamente");
            } catch (BibliotecaException e) {
                System.err.printf(ERROR, e.getMessage());
            }
            return false;
        }

        private boolean buscarLibroPorId() {
            try {
                System.out.printf(SEPARADOR, "BUSCAR LIBRO");
                String id = InputManager.leerTexto("ID del libro: ");
                Libro libro = biblioteca.obtenerLibroPorId(id);
                mostrarInformacionLibro(libro);
            } catch (BibliotecaException e) {
                System.err.printf(ERROR, e.getMessage());
            }
            return false;
        }

        private boolean crearUsuario() {
            try {
                System.out.printf(SEPARADOR, "CREAR USUARIO");
                String id = InputManager.leerTexto("ID: ");
                String nombre = InputManager.leerTexto("Nombre: ");

                Usuario usuario = new Usuario(id, nombre);
                biblioteca.crearUsuario(usuario);
                System.out.printf(EXITO, "Usuario creado exitosamente");
            } catch (BibliotecaException e) {
                System.err.printf(ERROR, e.getMessage());
            }
            return false;
        }

        private boolean prestarLibro() {
            try {
                System.out.printf(SEPARADOR, "PRESTAR LIBRO");
                String libroId = InputManager.leerTexto("ID del libro: ");
                String usuarioId = InputManager.leerTexto("ID del usuario: ");
                
                biblioteca.prestarLibro(libroId, usuarioId);
                System.out.printf(EXITO, "Libro prestado exitosamente");
            } catch (BibliotecaException e) {
                System.err.printf(ERROR, e.getMessage());
            }
            return false;
        }

        private boolean devolverLibro() {
            try {
                System.out.printf(SEPARADOR, "DEVOLVER LIBRO");
                String libroId = InputManager.leerTexto("ID del libro: ");
                String usuarioId = InputManager.leerTexto("ID del usuario: ");
                
                biblioteca.devolverLibro(libroId, usuarioId);
                System.out.printf(EXITO, "Libro devuelto exitosamente");
            } catch (BibliotecaException e) {
                System.err.printf(ERROR, e.getMessage());
            }
            return false;
        }

        private boolean mostrarInventario() {
            try {
                System.out.printf(SEPARADOR, "INVENTARIO DE LIBROS");
                List<Libro> libros = biblioteca.listarLibros();
                
                if (libros.isEmpty()) {
                    System.out.println("No hay libros en el inventario");
                } else {
                    System.out.println("\nLibros disponibles:");
                    libros.forEach(libro -> {
                        System.out.println("\nID: " + libro.getIsbn());
                        System.out.println("Título: " + libro.getTitulo());
                        System.out.println("Autor: " + libro.getAutor());
                        System.out.println("Ejemplares disponibles: " + libro.getEjemplaresDisponibles());
                        System.out.println("------------------------");
                    });
                }
            } catch (BibliotecaException e) {
                System.err.printf(ERROR, e.getMessage());
            }
            return false;
        }

        private boolean verPrestamosPorUsuario() {
            try {
                System.out.printf(SEPARADOR, "PRÉSTAMOS POR USUARIO");
                String usuarioId = InputManager.leerTexto("ID del usuario: ");
                List<Prestamo> prestamos = biblioteca.obtenerPrestamosPorUsuario(usuarioId);
                
                if (prestamos.isEmpty()) {
                    System.out.println("El usuario no tiene préstamos");
                } else {
                    System.out.println("\nPréstamos del usuario:");
                    prestamos.forEach(this::mostrarInformacionPrestamo);
                }
            } catch (BibliotecaException e) {
                System.err.printf(ERROR, e.getMessage());
            }
            return false;
        }

        private void mostrarInformacionLibro(Libro libro) {
            System.out.println("\nInformación del libro:");
            System.out.println("ID: " + libro.getIsbn());
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Autor: " + libro.getAutor());
        }

        private void mostrarInformacionPrestamo(Prestamo prestamo) {
            try {
                Libro libro = biblioteca.obtenerLibroPorId(prestamo.getLibroIsbn());
                System.out.println("\nLibro: " + libro.getTitulo());
                System.out.println("Fecha de préstamo: " + prestamo.getFechaPrestamo());
            } catch (BibliotecaException e) {
                System.err.printf(ERROR, e.getMessage());
            }
        }
    }

    private static class Menu {
        private final String titulo;
        private final List<OpcionMenu> opciones;

        public Menu(String titulo) {
            this.titulo = titulo;
            this.opciones = new ArrayList<>();
        }

        public Menu agregarOpcion(String descripcion, Supplier<Boolean> accion) {
            opciones.add(new OpcionMenu(descripcion, accion));
            return this;
        }

        public void mostrar() {
            boolean salir = false;
            while (!salir) {
                System.out.printf("\n=== %s ===\n", titulo);
                for (int i = 0; i < opciones.size(); i++) {
                    System.out.printf("%d. %s\n", i + 1, opciones.get(i).descripcion);
                }
                System.out.print("Seleccione una opción: ");

                try {
                    int opcion = InputManager.leerOpcion();
                    
                    switch (opcion) {
                        case 1:
                            opciones.get(0).ejecutar();
                            break;
                        case 2:
                            opciones.get(1).ejecutar();
                            break;
                        case 3:
                            opciones.get(2).ejecutar();
                            break;
                        case 4:
                            opciones.get(3).ejecutar();
                            break;
                        case 5:
                            opciones.get(4).ejecutar();
                            break;
                        case 6:
                            opciones.get(5).ejecutar();
                            break;
                        case 7:
                            opciones.get(6).ejecutar();
                            break;
                        case 8:
                            salir = opciones.get(7).ejecutar();
                            break;
                        default:
                            System.out.println("Opción no válida. Por favor seleccione una opción del 1 al " + opciones.size());
                            break;
                    }
                } catch (BibliotecaException e) {
                    System.err.printf("Error: %s\n", e.getMessage());
                }
            }
        }

        private static class OpcionMenu {
            private final String descripcion;
            private final Supplier<Boolean> accion;

            public OpcionMenu(String descripcion, Supplier<Boolean> accion) {
                this.descripcion = descripcion;
                this.accion = accion;
            }

            public boolean ejecutar() {
                return accion.get();
            }
        }
    }

    private static class InputManager {
        public static String leerTexto(String mensaje) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim();
            if (texto.isEmpty()) {
                throw new BibliotecaException("El campo no puede estar vacío");
            }
            return texto;
        }

        public static int leerOpcion() {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                throw new BibliotecaException("Debe ingresar un número válido");
            }
        }
    }
} 