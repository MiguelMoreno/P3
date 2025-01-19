import java.sql.*;
import java.util.Scanner;

public class GestionClientes {

	  /***************************************************/
	/* INSERTAR CLIENTE (con ID manual) */
	/***************************************************/
	private static void insertarCliente(Connection conn, int id, String nombre) {
		try {
		    conn.setAutoCommit(false); // Deshabilitar autocommit para transacciones manuales

		    // Insertar cliente en la tabla Cliente_Invitado con el ID proporcionado
		    String query = "INSERT INTO Cliente_Invitado (ID_Cliente_Invitado, Nombre) VALUES (?, ?)";
		    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
		        pstmt.setInt(1, id); // Asignar el ID manualmente
		        pstmt.setString(2, nombre);
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
        String query = "SELECT * FROM Cliente_Invitado WHERE Eliminado = 0";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Lista de clientes:");
            boolean hayClientes = false;
            while (rs.next()) {
                hayClientes = true;
                // Mostrar ID, Nombre del cliente
                System.out.println("ID: " + rs.getInt("ID_Cliente_Invitado") + ", Nombre: " + rs.getString("Nombre"));
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
    private static void actualizarCliente(Connection conn, int id, String nuevoNombre) {
        String query = "UPDATE Cliente_Invitado SET Nombre = ? WHERE ID_Cliente_Invitado = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nuevoNombre);
            pstmt.setInt(2, id);
            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                conn.commit();
                System.out.println("Cliente actualizado correctamente.");
            } else {
                System.out.println("No se encontró ningún cliente con el ID especificado.");
            }
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn); // Revertir en caso de error
        }
    }

    /***************************************************/
    /* ELIMINAR CLIENTE */
    /***************************************************/
    private static void eliminarCliente(Connection conn, int id) {
        try {
            // Comprobar si el cliente existe en la tabla Cliente_Invitado
            String queryCheckCliente = "SELECT COUNT(*) FROM Cliente_Invitado WHERE ID_Cliente_Invitado = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(queryCheckCliente)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("No se encontró ningún cliente con el ID especificado.");
                        return; // No existe el cliente, no hacemos nada
                    }
                }
            }

            // Deshabilitar autocommit para manejar la transacción manualmente
            conn.setAutoCommit(false);

            // Actualizar el campo Eliminado en la tabla Cliente_Invitado
            String queryCliente = "UPDATE Cliente_Invitado SET Eliminado = 1 WHERE ID_Cliente_Invitado = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(queryCliente)) {
                pstmt.setInt(1, id);
                int filasAfectadasCliente = pstmt.executeUpdate();
                if (filasAfectadasCliente > 0) {
                    conn.commit();
                    System.out.println("Cliente marcado como eliminado correctamente.");
                } else {
                    System.out.println("No se encontró ningún cliente con el ID especificado.");
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
    private static void asociarClienteConSuscripcion(Connection conn, int idCliente, int idSuscripcion, String codigo) {
        try {
            // Insertar en la tabla ClienteIncluyeSuscripcion_Invitacion
            String query = "INSERT INTO ClienteIncluyeSuscripcion_Invitacion (ID_Cliente_Invitado, ID_Suscripcion, Codigo, Fecha) " +
                           "VALUES (?, ?, ?, CURRENT_DATE)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, idCliente);
                pstmt.setInt(2, idSuscripcion);
                pstmt.setString(3, codigo);
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
            System.out.println("6. Volver al Menú Principal");
            System.out.print("Elige una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
					System.out.print("ID del Cliente: ");
					int id = scanner.nextInt();
					scanner.nextLine(); // Consumir el salto de línea

					System.out.print("Nombre del Cliente: ");
					String nombre = scanner.nextLine();
					insertarCliente(conn, id, nombre);  // Llamada con ID manual
					break;
                case 2:
                    listarClientes(conn);
                    break;
                case 3:
                    System.out.print("ID del Cliente a actualizar: ");
                    int idActualizar = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Nuevo Nombre: ");
                    String nuevoNombre = scanner.nextLine();
                    actualizarCliente(conn, idActualizar, nuevoNombre);
                    break;
                case 4:
                    System.out.print("ID del Cliente a eliminar: ");
                    int idEliminar = scanner.nextInt();
                    eliminarCliente(conn, idEliminar);
                    break;
                case 5:
                    System.out.print("ID del Cliente: ");
                    int idCliente = scanner.nextInt();
                    System.out.print("ID de la Suscripción: ");
                    int idSuscripcion = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Código de la Suscripción: ");
                    String codigo = scanner.nextLine();
                    asociarClienteConSuscripcion(conn, idCliente, idSuscripcion, codigo);
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
