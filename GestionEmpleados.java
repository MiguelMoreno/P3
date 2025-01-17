import java.sql.*;
import java.util.Scanner;

public class GestionEmpleados {

    /***************************************************/
    /* INSERTA EMPLEADO */
    /***************************************************/

    private static void insertarEmpleado(Connection conn, int id, String nombre, String correo, String rol) {
        // Verificar que el rol sea válido
        if (!rol.equalsIgnoreCase("Administrador") && !rol.equalsIgnoreCase("Entrenador")) {
            System.out.println("Error: El rol debe ser 'Administrador' o 'Entrenador'.");
            return;
        }
    
        // Iniciar la transacción
        try {
            conn.setAutoCommit(false); // Deshabilitar autocommit para manejar la transacción manualmente
    
            // Insertar en la tabla Empleado
            String query = "INSERT INTO Empleado (ID_Empleado, Nombre, Correo_Electronico, ROL) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, nombre);
                pstmt.setString(3, correo);
                pstmt.setString(4, rol);  // Insertar el rol
                pstmt.executeUpdate();
            }
    
            // Insertar en la tabla correspondiente (Administrador o Entrenador)
            if (rol.equalsIgnoreCase("Administrador")) {
                insertarAdministrador(conn, id);
            } else if (rol.equalsIgnoreCase("Entrenador")) {
                insertarEntrenador(conn, id);
            }
    
            // Si todo fue bien, confirmar la transacción
            conn.commit();
            System.out.println("Empleado insertado correctamente.");
        } catch (SQLException e) {
            // Si ocurre un error, revertir la transacción
            manejarErroresSQL(e);
            rollback(conn);
        } finally {
            try {
                conn.setAutoCommit(true); // Volver a habilitar el autocommit después de la transacción
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
            System.out.println("5. Volver al Menú Principal");
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
            System.err.println("Error específico: ID duplicado.");
        } else if (e.getErrorCode() == 20003) {
            System.err.println("Error específico: Nombre duplicado.");
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
