import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GestionClientes {

    /***************************************************/
    /* INSERTAR CLIENTE (con ID manual) */
    /***************************************************/
    private static void insertarCliente(Connection conn, int idSuscripcion, String codigo) {
        try {
            conn.setAutoCommit(false); // Deshabilitar autocommit para transacciones manuales

            // Insertar cliente en la tabla ClienteIncluyeSuscripcion_Invitacion
            String query = "INSERT INTO ClienteIncluyeSuscripcion_Invitacion (ID_Suscripcion, ID_Cliente_Invitado, Codigo, Fecha) VALUES (?, NULL, ?, CURRENT_DATE)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, idSuscripcion);
                pstmt.setString(2, codigo);
                pstmt.executeUpdate();
            }

            conn.commit(); // Confirmar transacción
            System.out.println("Cliente insertado correctamente.");
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn); // Revertir en caso de error
        } finally {
            try {
                conn.setAutoCommit(true); // Habilitar autocommit después de la transacción
            } catch (SQLException e) {
                manejarErroresSQL(e);
            }
        }
    }

    /***************************************************/
    /* LISTAR CLIENTES */
    /***************************************************/
    private static void listarClientes(Connection conn) {
        String query = "SELECT * FROM ClienteIncluyeSuscripcion_Invitacion WHERE Eliminado = 0";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Lista de clientes:");
            boolean hayClientes = false;
            while (rs.next()) {
                hayClientes = true;
                int idSuscripcion = rs.getInt("ID_SUSCRIPCION");
                int idCliente = rs.getInt("ID_CLIENTE");
                // Comprobar si ID_CLIENTE_INVITADO es NULL
                String clienteInvitado = rs.getString("ID_CLIENTE_INVITADO");
    
                // Si el valor es NULL, asignamos un mensaje adecuado
                if (clienteInvitado == null) {
                    clienteInvitado = "No hay Clientes asociados";
                }
    
                // Mostrar ID Suscripción, ID Cliente y Cliente Invitado
                System.out.println("ID Cliente: " + idCliente + ", ID Suscripción: " + idSuscripcion +
                                   ", Cliente Invitado: " + clienteInvitado);
            }
            if (!hayClientes) {
                System.out.println("No hay clientes activos para mostrar.");
            }
        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
    }       

    /***************************************************/
    /* ACTUALIZAR CLIENTE */
    /***************************************************/
    private static void actualizarCliente(Connection conn, int idSuscripcion, String nuevoCodigo) {
        String query = "UPDATE ClienteIncluyeSuscripcion_Invitacion SET Codigo = ? WHERE ID_Suscripcion = ? AND ID_Cliente_Invitado IS NULL";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nuevoCodigo);
            pstmt.setInt(2, idSuscripcion);
            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                conn.commit();
                System.out.println("Cliente actualizado correctamente.");
            } else {
                System.out.println("No se encontró ningún cliente con los ID especificados.");
            }
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn); // Revertir en caso de error
        }
    }

    public static void actualizarCorreoEmpleado(Connection conn, int idEmpleado, String nuevoCorreo) {
        // Expresión regular para validar el formato del correo electrónico
        String regexCorreo = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (!Pattern.matches(regexCorreo, nuevoCorreo)) {
            System.out.println("Error: El correo electrónico tiene un formato inválido.");
            return;
        }

        String query = "UPDATE Empleado SET Correo = ? WHERE ID_Empleado = ? AND Eliminado = 0";

        try {
            // Deshabilitar autocommit para manejar la transacción manualmente
            conn.setAutoCommit(false);

            // Intentar realizar la actualización
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, nuevoCorreo);
                pstmt.setInt(2, idEmpleado);

                int filasAfectadas = pstmt.executeUpdate();
                if (filasAfectadas > 0) {
                    conn.commit(); // Confirmar transacción
                    System.out.println("Correo actualizado correctamente.");
                } else {
                    System.out.println("No se encontró el empleado o está marcado como eliminado.");
                    conn.rollback(); // Revertir la transacción en caso de no encontrar el empleado
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar el correo del empleado: " + e.getMessage());
            rollback(conn); // Revertir en caso de error
        } finally {
            try {
                conn.setAutoCommit(true); // Restaurar autocommit
            } catch (SQLException e) {
                System.err.println("Error al restaurar autocommit: " + e.getMessage());
            }
        }
    }

    /***************************************************/
    /* ELIMINAR CLIENTE */
    /***************************************************/
    private static void eliminarCliente(Connection conn, int idSuscripcion) {
        try {
            // Comprobar si el cliente existe en la tabla ClienteIncluyeSuscripcion_Invitacion
            String queryCheckCliente = "SELECT COUNT(*) FROM ClienteIncluyeSuscripcion_Invitacion WHERE ID_Suscripcion = ? AND ID_Cliente_Invitado IS NULL";
            try (PreparedStatement pstmt = conn.prepareStatement(queryCheckCliente)) {
                pstmt.setInt(1, idSuscripcion);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("No se encontró ningún cliente con los ID especificados.");
                        return; // No existe el cliente, no hacemos nada
                    }
                }
            }

            // Deshabilitar autocommit para manejar la transacción manualmente
            conn.setAutoCommit(false);

            // Actualizar el campo Eliminado en la tabla ClienteIncluyeSuscripcion_Invitacion
            String queryCliente = "UPDATE ClienteIncluyeSuscripcion_Invitacion SET Eliminado = 1 WHERE ID_Suscripcion = ? AND ID_Cliente_Invitado IS NULL";
            try (PreparedStatement pstmt = conn.prepareStatement(queryCliente)) {
                pstmt.setInt(1, idSuscripcion);
                int filasAfectadasCliente = pstmt.executeUpdate();
                if (filasAfectadasCliente > 0) {
                    conn.commit();
                    System.out.println("Cliente marcado como eliminado correctamente.");
                } else {
                    System.out.println("No se encontró ningún cliente con los ID especificados.");
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
    /* ASOCIAR CLIENTE CON SUSCRIPCION */
    /***************************************************/
    private static void asociarClienteConSuscripcion(Connection conn, int idSuscripcion, String codigo) {
        try {
            // Insertar en la tabla ClienteIncluyeSuscripcion_Invitacion
            String query = "INSERT INTO ClienteIncluyeSuscripcion_Invitacion (ID_Suscripcion, ID_Cliente_Invitado, Codigo, Fecha) " +
                    "VALUES (?, NULL, ?, CURRENT_DATE)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, idSuscripcion);
                pstmt.setString(2, codigo);
                pstmt.executeUpdate();
            }

            conn.commit(); // Confirmar transacción
            System.out.println("Cliente asociado con la suscripción correctamente.");
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn); // Revertir en caso de error
        }
    }

    /***************************************************/
    /* MENU */
    /***************************************************/
    public static void mostrarMenuGestionClientes(Connection conn, Scanner scanner) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\nGestión de Clientes:");
            System.out.println("1. Insertar Cliente");
            System.out.println("2. Listar Clientes");
            System.out.println("3. Actualizar Cliente");
            System.out.println("4. Eliminar Cliente");
            System.out.println("5. Asociar Cliente con Suscripción");
            System.out.println("6. Actualizar Correo Electronico de un Cliente");
            System.out.println("7. Volver al Menú Principal");
            System.out.print("Elige una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    System.out.print("ID Cliente: ");
                    int idSuscripcion = scanner.nextInt();
                    scanner.nextLine(); // Consumir el salto de línea
                    System.out.print("Código de la Suscripción: ");
                    String codigo = scanner.nextLine();
                    insertarCliente(conn, idSuscripcion, codigo);  // Llamada con datos modificados
                    break;
                case 2:
                    listarClientes(conn);
                    break;
                case 3:
                    System.out.print("ID Cliente a actualizar: ");
                    int idSuscripcionActualizar = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Nuevo Código: ");
                    String nuevoCodigo = scanner.nextLine();
                    actualizarCliente(conn, idSuscripcionActualizar, nuevoCodigo);
                    break;
                case 4:
                    System.out.print("ID Cliente a eliminar: ");
                    int idSuscripcionEliminar = scanner.nextInt();
                    eliminarCliente(conn, idSuscripcionEliminar);
                    break;
                case 5:
                    System.out.print("ID Cliente: ");
                    int idSuscripcionAsociar = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Código de la Suscripción: ");
                    String codigoAsociar = scanner.nextLine();
                    asociarClienteConSuscripcion(conn, idSuscripcionAsociar, codigoAsociar);
                    break;
                case 6:
                    System.out.print("ID Cliente: ");
                    int idCliente = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Nuevo Correo Electronico: ");
                    String nuevoCorreo = scanner.nextLine();
                    asociarClienteConSuscripcion(conn, idCliente, nuevoCorreo);
                    break;
                case 7:
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
        if (e.getErrorCode() == 20001) {
            System.err.println("Error: El código de suscripción ya existe. " + e.getMessage());
        } else if (e.getErrorCode() == 20002) {
            System.err.println("Error: La fecha de suscripción no puede ser anterior a hoy. " + e.getMessage());
        } else if (e.getErrorCode() == 20003) {
            System.err.println("Error: El cliente ya está asociado a esta suscripción. " + e.getMessage());
        } else {
            System.err.println("Error general en la inserción: " + e.getMessage());
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
