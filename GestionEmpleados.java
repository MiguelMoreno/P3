// Práctica 3 DDSI 3ºA-3 Grupo4
/* Por:
Miguel Moreno Murcia
Santiago Romero Alonso
Alberto García Lara
Miguel Álvarez de Cienfuegos Cortés
*/

import java.sql.*;
import java.util.Scanner;

public class GestionEmpleados {

    /***************************************************/
    /* INSERTA EMPLEADO */
    /***************************************************/

    private static void insertarEmpleado(Connection conn, int id, String nombre, String correo, String rol) {
        try {
            conn.setAutoCommit(false);
    
            // Insertar en la tabla Empleado
            String query = "INSERT INTO Empleado (ID_Empleado, Nombre, Correo_Electronico, ROL) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, nombre);
                pstmt.setString(3, correo);
                pstmt.setString(4, rol);
                pstmt.executeUpdate();
            }
    
            // Insertar en la tabla correspondiente (Administrador o Entrenador)
            if (rol.equalsIgnoreCase("Administrador")) {
                insertarAdministrador(conn, id);
            } else if (rol.equalsIgnoreCase("Entrenador")) {
                insertarEntrenador(conn, id);
            }
    
            conn.commit();
            System.out.println("Empleado insertado correctamente.");
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                manejarErroresSQL(e);
            }
        }
    }    
    
    private static void insertarAdministrador(Connection conn, int idEmpleado) {
        String query = "INSERT INTO Administrador (ID_Empleado) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idEmpleado);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        }
    }
    
    private static void insertarEntrenador(Connection conn, int idEmpleado) {
        String query = "INSERT INTO Entrenador (ID_Empleado) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idEmpleado);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        }
    }

    /***************************************************/
    /* lISTAR EMPLEADOS */
    /***************************************************/

    private static void listarEmpleados(Connection conn) {
        String query = "SELECT * FROM Empleado WHERE Eliminado = 0";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Lista de empleados:");
            boolean hayEmpleados = false;
            while (rs.next()) {
                hayEmpleados = true;
                // Mostrar ID, Nombre, Correo Electrónico y Rol
                System.out.println("ID: " + rs.getInt("ID_Empleado") + ", Nombre: " + rs.getString("Nombre") + 
                                   ", Correo Electrónico: " + rs.getString("Correo_Electronico") + 
                                   ", Rol: " + rs.getString("ROL"));
            }
            if (!hayEmpleados) {
                System.out.println("No hay empleados activos para mostrar.");
            }
        } catch (SQLException e) {
            System.err.println("Error al listar empleados: " + e.getMessage());
        }
    }
    

    /***************************************************/
    /* ACTUALIZAR EMPLEADO */
    /***************************************************/

    private static void actualizarEmpleado(Connection conn, int id, String nuevoNombre, String nuevoRol) {
        String query = "UPDATE Empleado SET nombre = ?, rol = ? WHERE idEmpleado = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nuevoNombre);
            pstmt.setString(2, nuevoRol);
            pstmt.setInt(3, id);
            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                conn.commit();
                System.out.println("Empleado actualizado correctamente.");
            } else {
                System.out.println("No se encontró ningún empleado con el ID especificado.");
            }
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        }
    }

    /***************************************************/
    /* ELIMINAR EMPLEADO */
    /***************************************************/

    private static void eliminarEmpleado(Connection conn, int id) {
        try {
            // Comprobar si el empleado existe en la tabla Empleado
            String queryCheckEmpleado = "SELECT COUNT(*) FROM Empleado WHERE ID_Empleado = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(queryCheckEmpleado)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("No se encontró ningún empleado con el ID especificado.");
                        return; // No existe el empleado, no hacemos nada
                    }
                }
            }
    
            // Deshabilitar autocommit para manejar la transacción manualmente
            conn.setAutoCommit(false);
    
            // Actualizar el campo Eliminado en la tabla Administrador si existe el registro
            String queryAdmin = "UPDATE Administrador SET Eliminado = 1 WHERE ID_Empleado = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(queryAdmin)) {
                pstmt.setInt(1, id);
                //int filasAfectadasAdmin = pstmt.executeUpdate();
                //System.out.println("Filas actualizadas en Administrador: " + filasAfectadasAdmin);
            }
    
            // Actualizar el campo Eliminado en la tabla Entrenador si existe el registro
            String queryEntrenador = "UPDATE Entrenador SET Eliminado = 1 WHERE ID_Empleado = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(queryEntrenador)) {
                pstmt.setInt(1, id);
                //int filasAfectadasEntrenador = pstmt.executeUpdate();
                //System.out.println("Filas actualizadas en Entrenador: " + filasAfectadasEntrenador);
            }
    
            // Actualizar el campo Eliminado en la tabla Empleado
            String queryEmpleado = "UPDATE Empleado SET Eliminado = 1 WHERE ID_Empleado = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(queryEmpleado)) {
                pstmt.setInt(1, id);
                int filasAfectadasEmpleado = pstmt.executeUpdate();
                //System.out.println("Filas actualizadas en Empleado: " + filasAfectadasEmpleado);
    
                if (filasAfectadasEmpleado > 0) {
                    conn.commit();
                    System.out.println("Empleado marcado como eliminado correctamente.");
                } else {
                    System.out.println("No se encontró ningún empleado con el ID especificado.");
                }
            }
    
        } catch (SQLException e) {
            System.err.println("Error durante la actualización del campo 'Eliminado': " + e.getMessage());
            rollback(conn);
        } finally {
            try {
                conn.setAutoCommit(true); // Volver a habilitar autocommit después de la transacción
            } catch (SQLException e) {
                System.err.println("Error al restaurar autocommit: " + e.getMessage());
            }
        }
    }
    
    /***************************************************/
    /* CREAR RUTINA PARA CLIENTE */
    /***************************************************/

    private static void insertarRutina(Connection conn, int idEmpleado, int idCliente, String descripcion, String[] ejercicios, int duracion) {
        try {
            // Comprobar si el empleado es un Administrador
            if (!esAdministrador(conn, idEmpleado)) {
                System.out.println("El empleado no tiene permisos de administrador.");
                return;  // Terminamos la operación si no es administrador
            }
    
            // Verificar si el cliente existe
            if (!existeCliente(conn, idCliente)) {
                System.out.println("No existe un cliente con el ID especificado.");
                return;  // Terminamos la operación si el cliente no existe
            }
    
            // Verificar si ya existe una rutina con el mismo nombre para el cliente
            if (rutinaExistente(conn, idCliente, descripcion)) {
                System.out.println("Ya existe una rutina con ese nombre para este cliente.");
                return;  // Terminamos la operación si la rutina ya existe
            }
    
            // Validar que haya al menos un ejercicio
            if (ejercicios == null || ejercicios.length == 0) {
                System.out.println("La rutina debe contener al menos un ejercicio.");
                return;  // Terminamos la operación si no hay ejercicios
            }
    
            // Validar que la duración no supere los 180 minutos
            if (duracion > 180) {
                System.out.println("La duración máxima de la rutina es de 180 minutos.");
                return;  // Terminamos la operación si la duración es inválida
            }
    
            // Crear el tipo VARRAY de ejercicios
            Array ejerciciosArray = conn.createArrayOf("VARCHAR", ejercicios);
    
            // Insertar la rutina en la tabla TieneRutina
            String queryInsertRutina = "INSERT INTO TieneRutina (ID_Cliente, Ejercicios, Duracion, Descripcion) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(queryInsertRutina)) {
                pstmt.setInt(1, idCliente);
                pstmt.setArray(2, ejerciciosArray);
                pstmt.setInt(3, duracion);
                pstmt.setString(4, descripcion);
                pstmt.executeUpdate();
            }
    
            conn.commit();
            System.out.println("Rutina insertada correctamente.");
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        } finally {
            try {
                conn.setAutoCommit(true); // Restaurar el autocommit
            } catch (SQLException e) {
                manejarErroresSQL(e);
            }
        }
    }

    private static boolean esAdministrador(Connection conn, int idEmpleado) {
        String query = "SELECT COUNT(*) FROM Empleado WHERE ID_Empleado = ? AND ROL = 'Administrador' AND Eliminado = 0";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idEmpleado);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;  // El empleado es un Administrador
                }
            }
        } catch (SQLException e) {
            manejarErroresSQL(e);
        }
        return false;  // No es un Administrador
    }
    
    private static boolean existeCliente(Connection conn, int idCliente) {
        String query = "SELECT COUNT(*) FROM ClienteIncluyeSuscripcion_Invitacion WHERE ID_Cliente = ? AND Eliminado = 0";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;  // El cliente existe
                }
            }
        } catch (SQLException e) {
            manejarErroresSQL(e);
        }
        return false;  // El cliente no existe
    }

    private static boolean rutinaExistente(Connection conn, int idCliente, String descripcion) {
        String query = "SELECT COUNT(*) FROM TieneRutina WHERE ID_Cliente = ? AND Descripcion = ? AND Eliminado = 0";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente);
            pstmt.setString(2, descripcion);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;  // Ya existe una rutina con ese nombre
                }
            }
        } catch (SQLException e) {
            manejarErroresSQL(e);
        }
        return false;  // No existe la rutina
    }    

    /***************************************************/
    /* MENU */
    /***************************************************/

    public static void mostrarMenuGestionEmpleados(Connection conn, Scanner scanner) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\nGestión de Empleados:");
            System.out.println("1. Insertar Empleado");
            System.out.println("2. Listar Empleados");
            System.out.println("3. Actualizar Empleado");
            System.out.println("4. Eliminar Empleado");
            System.out.println("5. Crear Rutina");
            System.out.println("6. Volver al Menú Principal");
            System.out.print("Elige una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    System.out.print("ID: ");
                    int id = scanner.nextInt();
                    scanner.nextLine(); // Consumir el salto de línea

                    System.out.print("Nombre: ");
                    String nombre = scanner.nextLine();

                    System.out.print("Correo Electrónico: ");
                    String correo = scanner.nextLine(); // Pedimos el correo

                    System.out.print("Rol (Administrador o Entrenador): ");
                    String rol = scanner.nextLine();

                    insertarEmpleado(conn, id, nombre, correo, rol);
                    break;
                case 2:
                    listarEmpleados(conn);
                    break;
                case 3:
                    System.out.print("ID del Empleado a actualizar: ");
                    int idActualizar = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Nuevo Nombre: ");
                    String nuevoNombre = scanner.nextLine();
                    System.out.print("Nuevo Rol: ");
                    String nuevoRol = scanner.nextLine();
                    actualizarEmpleado(conn, idActualizar, nuevoNombre, nuevoRol);
                    break;
                case 4:
                    System.out.print("ID del Empleado a eliminar: ");
                    int idEliminar = scanner.nextInt();
                    eliminarEmpleado(conn, idEliminar);
                    break;
                case 5:
                    System.out.print("ID del Empleado (Administrador): ");
                    int idEmpleado = scanner.nextInt();
                    scanner.nextLine(); // Consumir el salto de línea

                    System.out.print("ID del Cliente: ");
                    int idCliente = scanner.nextInt();
                    scanner.nextLine(); // Consumir el salto de línea

                    System.out.print("Descripción de la rutina: ");
                    String descripcion = scanner.nextLine();

                    System.out.print("Duración de la rutina (en minutos): ");
                    int duracion = scanner.nextInt();
                    scanner.nextLine(); // Consumir el salto de línea

                    System.out.println("Introduce los ejercicios (separados por coma): ");
                    String ejerciciosInput = scanner.nextLine();
                    String[] ejercicios = ejerciciosInput.split(",");

                    insertarRutina(conn, idEmpleado, idCliente, descripcion, ejercicios, duracion);
                    break;
                case 6:
                    salir = true;
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    /***************************************************/
    /* MANEJO DE ERRORES */
    /***************************************************/

    private static void manejarErroresSQL(SQLException e) {
        System.err.println("Error: " + e.getMessage());
        if (e.getErrorCode() == 20001) {
            System.err.println("Error específico: Rol inválido.");
        } else if (e.getErrorCode() == 20002) {
            System.err.println("Error específico: Nombre duplicado.");
        } else if (e.getErrorCode() == 20003) {
            System.err.println("Error específico: Correo duplicado.");
        } else if (e.getErrorCode() == 20004) {
            System.err.println("Error específico: ID duplicado.");
        }
    }    

    private static void rollback(Connection conn) {
        try {
            conn.rollback();
            System.out.println("Transacción revertida.");
        } catch (SQLException rollbackEx) {
            System.err.println("Error al revertir la transacción: " + rollbackEx.getMessage());
        }
    }
}
