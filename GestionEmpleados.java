import java.sql.*;
import java.util.Scanner;

public class GestionEmpleados {

    private static void insertarEmpleado(Connection conn, int id, String nombre, String rol) {
        // Verificar que el rol sea válido
        if (!rol.equalsIgnoreCase("Administrador") && !rol.equalsIgnoreCase("Entrenador")) {
            System.out.println("Error: El rol debe ser 'Administrador' o 'Entrenador'.");
            return;
        }
    
        String query = "INSERT INTO Empleado (idEmpleado, nombre, rol) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, nombre);
            pstmt.setString(3, rol);
            pstmt.executeUpdate();
            conn.commit();
            System.out.println("Empleado insertado correctamente.");
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        }
    }
    

    private static void listarEmpleados(Connection conn) {
        String query = "SELECT * FROM Empleado";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Lista de empleados:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("idEmpleado") + ", Nombre: " + rs.getString("nombre") + ", Rol: " + rs.getString("rol"));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar empleados: " + e.getMessage());
        }
    }

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

    private static void eliminarEmpleado(Connection conn, int id) {
        String query = "DELETE FROM Empleado WHERE idEmpleado = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                conn.commit();
                System.out.println("Empleado eliminado correctamente.");
            } else {
                System.out.println("No se encontró ningún empleado con el ID especificado.");
            }
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        }
    }

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
                    scanner.nextLine();
                    System.out.print("Nombre: ");
                    String nombre = scanner.nextLine();
                    System.out.print("Rol: ");
                    String rol = scanner.nextLine();
                    insertarEmpleado(conn, id, nombre, rol);
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
