// Práctica 3 DDSI 3ºA-3 Grupo4
/* Por:
Miguel Moreno Murcia
Santiago Romero Alonso
Alberto García Lara
Miguel Álvarez de Cienfuegos Cortés
*/

import java.sql.*;
import java.util.Scanner;

public class GestionActividades {

    /***************************************************/
    /* INSERTA ACTIVIDAD */
    /***************************************************/
    private static void insertarActividad(Connection conn, int idEntrenador, String nombre, String descripcion, java.sql.Date fechaInicio, java.sql.Date fechaFin, int plazas) {
        try {
            conn.setAutoCommit(false);
    
            // Validar que el número de plazas sea mayor a 0
            if (plazas <= 0) {
                System.out.println("El número de plazas debe ser mayor a 0.");
                return;
            }
    
            // Consulta para insertar la actividad con el nuevo campo 'Plazas'
            String query = "INSERT INTO Escoge_Actividad (ID_Entrenador, Nombre, Descripcion, Fecha_Ini, Fecha_Fin, Plazas, Eliminado) VALUES (?, ?, ?, ?, ?, ?, 0)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, idEntrenador);
                pstmt.setString(2, nombre);
                pstmt.setString(3, descripcion);
                pstmt.setDate(4, fechaInicio);
                pstmt.setDate(5, fechaFin);
                pstmt.setInt(6, plazas);  // Establecer el valor de 'Plazas'
                pstmt.executeUpdate();
            }
    
            conn.commit();
            System.out.println("Actividad insertada correctamente.");
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        } finally {
            restaurarAutoCommit(conn);
        }
    }    

    /***************************************************/
    /* LISTAR ACTIVIDADES */
    /***************************************************/
    private static void listarActividades(Connection conn) {
        String query = "SELECT * FROM Escoge_Actividad WHERE Eliminado = 0";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Lista de actividades:");
            boolean hayActividades = false;
            while (rs.next()) {
                hayActividades = true;
                System.out.println("ID: " + rs.getInt("ID_Actividad") +
                        ", Nombre: " + rs.getString("Nombre") +
                        ", Descripción: " + rs.getString("Descripcion") +
                        ", Fecha Inicio: " + rs.getDate("Fecha_Ini") +
                        ", Fecha Fin: " + rs.getDate("Fecha_Fin"));
            }
            if (!hayActividades) {
                System.out.println("No hay actividades activas para mostrar.");
            }
        } catch (SQLException e) {
            System.err.println("Error al listar actividades: " + e.getMessage());
        }
    }

    /***************************************************/
    /* ACTUALIZAR ACTIVIDADES */
    /***************************************************/
    private static void actualizarActividad(Connection conn, int idActividad, String nuevoNombre, String nuevaDescripcion, java.sql.Date nuevaFechaInicio, java.sql.Date nuevaFechaFin) {
        String query = "UPDATE Escoge_Actividad SET Nombre = ?, Descripcion = ?, Fecha_Ini = ?, Fecha_Fin = ? WHERE ID_Actividad = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nuevoNombre);
            pstmt.setString(2, nuevaDescripcion);
            pstmt.setDate(3, nuevaFechaInicio);
            pstmt.setDate(4, nuevaFechaFin);
            pstmt.setInt(5, idActividad);

            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                conn.commit();
                System.out.println("Actividad actualizada correctamente.");
            } else {
                System.out.println("No se encontró ninguna actividad con el ID especificado.");
            }
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        } finally {
            restaurarAutoCommit(conn);
        }
    }

    /***************************************************/
    /* ELIMINAR ACTIVIDADES */
    /***************************************************/
    private static void eliminarActividad(Connection conn, int idActividad) {
        try {
            if (!existeActividad(conn, idActividad)) {
                System.out.println("No se encontró ninguna actividad con el ID especificado.");
                return;
            }

            conn.setAutoCommit(false);
            String query = "UPDATE Escoge_Actividad SET Eliminado = 1 WHERE ID_Actividad = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, idActividad);
                int filasAfectadas = pstmt.executeUpdate();

                if (filasAfectadas > 0) {
                    conn.commit();
                    System.out.println("Actividad marcada como eliminada correctamente.");
                } else {
                    System.out.println("No se encontró ninguna actividad con el ID especificado.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error durante la eliminación de la actividad: " + e.getMessage());
            rollback(conn);
        } finally {
            restaurarAutoCommit(conn);
        }
    }

    /***************************************************/
    /* INSCRIBIR CLIENTE EN ACTIVIDAD */
    /***************************************************/
    private static void inscribirEnActividad(Connection conn, int idCliente, int idActividad) {
        try {
            // Validar que la actividad exista y tenga plazas disponibles
            String queryValidarActividad = 
                "SELECT Plazas " +
                "FROM Escoge_Actividad " +
                "WHERE ID_Actividad = ? AND Eliminado = 0";
            
            int plazasDisponibles = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(queryValidarActividad)) {
                pstmt.setInt(1, idActividad);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        plazasDisponibles = rs.getInt("Plazas");
                    } else {
                        System.out.println("La actividad no existe o ha sido eliminada.");
                        return;
                    }
                }
            }
            
            if (plazasDisponibles <= 0) {
                System.out.println("No hay plazas disponibles para esta actividad.");
                return;
            }
            
            // Verificar que el cliente no esté ya inscrito en la actividad
            String queryVerificarInscripcion = 
                "SELECT COUNT(*) " +
                "FROM Inscribe_en_Actividad " +
                "WHERE ID_Actividad = ? AND ID_Cliente = ? AND Eliminado = 0";
            
            try (PreparedStatement pstmt = conn.prepareStatement(queryVerificarInscripcion)) {
                pstmt.setInt(1, idActividad);
                pstmt.setInt(2, idCliente);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("El cliente ya está inscrito en esta actividad.");
                        return;
                    }
                }
            }
            
            // Realizar la inscripción
            conn.setAutoCommit(false);
            
            String queryInscribir = 
                "INSERT INTO Inscribe_en_Actividad (ID_Actividad, ID_Cliente) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(queryInscribir)) {
                pstmt.setInt(1, idActividad);
                pstmt.setInt(2, idCliente);
                pstmt.executeUpdate();
            }
            
            // Reducir las plazas disponibles de la actividad
            String queryActualizarPlazas = 
                "UPDATE Escoge_Actividad " +
                "SET Plazas = Plazas - 1 " +
                "WHERE ID_Actividad = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(queryActualizarPlazas)) {
                pstmt.setInt(1, idActividad);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            System.out.println("El cliente se inscribió correctamente en la actividad.");
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        } finally {
            restaurarAutoCommit(conn);
        }
    }

    /***************************************************/
    /* MODIFICAR ENTRENADOR DE ACTIVIDAD */
    /***************************************************/

    private static void modificarEntrenadorActividad(Connection conn, int idEmpleado, int idActividad, int idNuevoEntrenador) {
        try {
            // Validar permisos de administrador
            if (!esAdministrador(conn, idEmpleado)) {
                System.out.println("El empleado no tiene permisos de administrador.");
                return;
            }
    
            // Verificar que la actividad exista
            String queryValidarActividad = 
                "SELECT COUNT(*) " +
                "FROM Escoge_Actividad " +
                "WHERE ID_Actividad = ? AND Eliminado = 0";
            
            try (PreparedStatement pstmt = conn.prepareStatement(queryValidarActividad)) {
                pstmt.setInt(1, idActividad);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        System.out.println("La actividad no existe o ha sido eliminada.");
                        return;
                    }
                }
            }
    
            // Verificar que el nuevo entrenador esté registrado y disponible en el horario de la actividad
            String queryValidarEntrenador = 
                "SELECT COUNT(*) " +
                "FROM Entrenador " +
                "WHERE ID_Entrenador = ? AND Eliminado = 0";
            
            try (PreparedStatement pstmt = conn.prepareStatement(queryValidarEntrenador)) {
                pstmt.setInt(1, idNuevoEntrenador);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        System.out.println("El entrenador no está registrado o ha sido eliminado.");
                        return;
                    }
                }
            }
    
            // Actualizar el entrenador asignado a la actividad
            conn.setAutoCommit(false);
    
            String queryActualizarEntrenador = 
                "UPDATE Escoge_Actividad " +
                "SET ID_Entrenador = ? " +
                "WHERE ID_Actividad = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(queryActualizarEntrenador)) {
                pstmt.setInt(1, idNuevoEntrenador);
                pstmt.setInt(2, idActividad);
                pstmt.executeUpdate();
            }
    
            conn.commit();
            System.out.println("El entrenador ha sido actualizado correctamente para la actividad con ID: " + idActividad);
        } catch (SQLException e) {
            manejarErroresSQL(e);
            rollback(conn);
        } finally {
            restaurarAutoCommit(conn);
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
    
    /***************************************************/
    /* MENU DE GESTIÓN DE ACTIVIDADES */
    /***************************************************/
    public static void mostrarMenuGestionActividades(Connection conn, Scanner scanner) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\nGestión de Actividades:");
            System.out.println("1. Insertar Actividad");
            System.out.println("2. Listar Actividades");
            System.out.println("3. Actualizar Actividad");
            System.out.println("4. Eliminar Actividad");
            System.out.println("5. Inscribir un Cliente en una Actividad");
            System.out.println("6. Modificar Entrenador de una Actividad");
            System.out.println("7. Volver al Menú Principal");
            System.out.print("Elige una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    System.out.print("ID del Entrenador: ");
                    int idEntrenador = scanner.nextInt();
                    scanner.nextLine();

                    System.out.print("Nombre de la Actividad: ");
                    String nombre = scanner.nextLine();

                    System.out.print("Descripción de la Actividad: ");
                    String descripcion = scanner.nextLine();

                    System.out.print("Fecha de Inicio (yyyy-MM-dd): ");
                    String fechaInicioStr = scanner.nextLine();
                    System.out.print("Fecha de Fin (yyyy-MM-dd): ");
                    String fechaFinStr = scanner.nextLine();

                    System.out.println("Número de plazas: ");
                    int plazas = scanner.nextInt();
                    try {
                        java.sql.Date fechaInicio = java.sql.Date.valueOf(fechaInicioStr);
                        java.sql.Date fechaFin = java.sql.Date.valueOf(fechaFinStr);
                        insertarActividad(conn, idEntrenador, nombre, descripcion, fechaInicio, fechaFin, plazas);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Formato de fecha inválido. Usa el formato yyyy-MM-dd.");
                    }
                    break;
                case 2:
                    listarActividades(conn);
                    break;
                case 3:
                    System.out.print("ID de la Actividad a actualizar: ");
                    int idActividadActualizar = scanner.nextInt();
                    scanner.nextLine();

                    System.out.print("Nuevo Nombre de la Actividad: ");
                    String nuevoNombre = scanner.nextLine();

                    System.out.print("Nueva Descripción de la Actividad: ");
                    String nuevaDescripcion = scanner.nextLine();

                    System.out.print("Nueva Fecha de Inicio (yyyy-MM-dd): ");
                    String nuevaFechaInicioStr = scanner.nextLine();
                    System.out.print("Nueva Fecha de Fin (yyyy-MM-dd): ");
                    String nuevaFechaFinStr = scanner.nextLine();

                    try {
                        java.sql.Date nuevaFechaInicio = java.sql.Date.valueOf(nuevaFechaInicioStr);
                        java.sql.Date nuevaFechaFin = java.sql.Date.valueOf(nuevaFechaFinStr);
                        actualizarActividad(conn, idActividadActualizar, nuevoNombre, nuevaDescripcion, nuevaFechaInicio, nuevaFechaFin);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Formato de fecha inválido. Usa el formato yyyy-MM-dd.");
                    }
                    break;
                case 4:
                    System.out.print("ID de la Actividad a eliminar: ");
                    int idActividadEliminar = scanner.nextInt();
                    eliminarActividad(conn, idActividadEliminar);
                    break;
                case 5:
                    System.out.print("ID del Cliente: ");
                    int idCliente = scanner.nextInt();
                    System.out.print("ID de la Actividad: ");
                    int idActividad = scanner.nextInt();
                    inscribirEnActividad(conn, idCliente, idActividad);
                    break;
                case 6:
                    System.out.println("Modificar entrenador de actividad");
                    System.out.print("ID del Empleado (Administrador): ");
                    int idEmpleado = scanner.nextInt();
                    System.out.print("ID de la Actividad: ");
                    int idActividad1 = scanner.nextInt();
                    System.out.print("ID del Nuevo Entrenador: ");
                    int idNuevoEntrenador = scanner.nextInt();
                    modificarEntrenadorActividad(conn, idEmpleado, idActividad1, idNuevoEntrenador);
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
	    System.err.println("Error SQL: " + e.getMessage());
	    System.err.println("Código de error: " + e.getErrorCode());
	
	    // Mensajes de error específicos para tu sistema
	    switch (e.getErrorCode()) {
	        case 1: // Código de error para clave única duplicada en Oracle
	            System.err.println("Error específico: Clave única duplicada.");
	            break;
	        case 2291: // Error de restricción de clave foránea en Oracle
	            System.err.println("Error específico: Referencia a una clave foránea inexistente.");
	            break;
	        case 2292: // Error de eliminación de clave referenciada en Oracle
	            System.err.println("Error específico: No se puede eliminar porque está referenciado en otra tabla.");
	            break;
            case 20001:
                System.err.println("Error específico: El ID del entrenador no existe en la tabla Entrenador.");
                break;
            case 20002:
                System.err.println("Error específico: La fecha de inicio debe ser menor o igual a la fecha de fin.");
                break;
            case 20003:
                System.err.println("El número de plazas debe ser mayor a 0.");
                break;
            case 20004:
                System.err.println("El nombre de la actividad ya existe y no ha sido eliminado.");
                break;
	        default:
	            System.err.println("Consulta la documentación del error para más detalles.");
	    }
	}

	/***************************************************/
	/* ROLLBACK */
	/***************************************************/
	private static void rollback(Connection conn) {
	    try {
	        if (conn != null && !conn.getAutoCommit()) {
	            conn.rollback();
	            System.out.println("Transacción revertida correctamente.");
	        }
	    } catch (SQLException e) {
	        System.err.println("Error al realizar el rollback: " + e.getMessage());
	    }
	}
		
	/***************************************************/
	/* RESTAURAR AUTOCOMMIT */
	/***************************************************/
	private static void restaurarAutoCommit(Connection conn) {
		try {
			if (conn != null) {
				conn.setAutoCommit(true);
				System.out.println("AutoCommit restaurado correctamente.");
			}
		} catch (SQLException e) {
			System.err.println("Error al restaurar AutoCommit: " + e.getMessage());
		}
	}

	/***************************************************/
	/* VERIFICAR SI LA ACTIVIDAD EXISTE */
	/***************************************************/
	private static boolean existeActividad(Connection conn, int idActividad) {
		String query = "SELECT COUNT(*) FROM Escoge_Actividad WHERE ID_Actividad = ? AND Eliminado = 0";
		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, idActividad);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error al verificar existencia de la actividad: " + e.getMessage());
		}
		return false;
	}
}
    
