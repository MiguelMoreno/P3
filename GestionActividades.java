// Práctica 3 DDSI 3ºA-3 Grupo4
/* Por:
Miguel Moreno Murcia
Santiago Romero Alonso
Alberto García Lara
Miguel Álvarez de Cienfuegos Cortés
*/

import java.sql.*;
import java.util.Scanner;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class GestionActividades{

     /***************************************************/
     /* INSERTA ACTIVIDAD */
     /***************************************************/
     
	private static void insertarActividad(Connection conn, int idEntrenador, String nombre, String descripcion, Date fechaInicio, Date fechaFin) {
        try {
            conn.setAutoCommit(false);

            // Consulta para insertar en la tabla Escoge_Actividad
            String query = "INSERT INTO Escoge_Actividad (ID_Entrenador, Nombre, Descripcion, Fecha_Ini, Fecha_Fin, Eliminado) VALUES (?, ?, ?, ?, ?, 0)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, idEntrenador);
                pstmt.setString(2, nombre);
                pstmt.setString(3, descripcion);
                pstmt.setDate(4, fechaInicio);
                pstmt.setDate(5, fechaFin);
                pstmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Actividad insertada correctamente.");
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
    	        // Mostrar ID, Nombre, Descripción, Fecha de Inicio y Fecha de Fin
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
	private static void actualizarActividad(Connection conn, int idActividad, String nuevoNombre, String nuevaDescripcion, Date nuevaFechaInicio, Date nuevaFechaFin) {
	    String query = "UPDATE Escoge_Actividad SET Nombre = ?, Descripcion = ?, Fecha_Ini = ?, Fecha_Fin = ? WHERE ID_Actividad = ?";
	    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setString(1, nuevoNombre);
	        pstmt.setString(2, nuevaDescripcion);
	        pstmt.setDate(3, new java.sql.Date(nuevaFechaInicio.getTime()));
	        pstmt.setDate(4, new java.sql.Date(nuevaFechaFin.getTime()));
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
	        rollback(conn); // Revertir en caso de error
	    }
	}
	
     /***************************************************/
     /* ELIMINAR ACTIVIDADES */
     /***************************************************/

	private static void eliminarActividad(Connection conn, int idActividad) {
    		try {
        		// Comprobar si la actividad existe en la tabla Escoge_Actividad
        		String queryCheckActividad = "SELECT COUNT(*) FROM Escoge_Actividad WHERE ID_Actividad = ?";
        		try (PreparedStatement pstmt = conn.prepareStatement(queryCheckActividad)) {
        		    pstmt.setInt(1, idActividad);
        		    try (ResultSet rs = pstmt.executeQuery()) {
        		        if (rs.next() && rs.getInt(1) == 0) {
        		            System.out.println("No se encontró ninguna actividad con el ID especificado.");
        		            return; // No existe la actividad, no hacemos nada
        		        }
        		    }
        		}
	
        		// Deshabilitar autocommit para manejar la transacción manualmente
        		conn.setAutoCommit(false);
		
        		// Actualizar el campo Eliminado en la tabla Escoge_Actividad
        		String queryEliminarActividad = "UPDATE Escoge_Actividad SET Eliminado = 1 WHERE ID_Actividad = ?";
        		try (PreparedStatement pstmt = conn.prepareStatement(queryEliminarActividad)) {
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
	        System.out.println("5. Volver al Menú Principal");
	        System.out.print("Elige una opción: ");

	        int opcion = scanner.nextInt();
	        scanner.nextLine(); // Consumir el salto de línea
	
	        switch (opcion) {
	            case 1:
	                System.out.print("ID del Entrenador: ");
	                int idEntrenador = scanner.nextInt();
	                scanner.nextLine(); // Consumir el salto de línea

	                System.out.print("Nombre de la Actividad: ");
	                String nombre = scanner.nextLine();
	
	                System.out.print("Descripción de la Actividad: ");
	                String descripcion = scanner.nextLine();
	
	                System.out.print("Fecha de Inicio (yyyy-MM-dd): ");
	                String fechaInicioStr = scanner.nextLine();
	                System.out.print("Fecha de Fin (yyyy-MM-dd): ");
	                String fechaFinStr = scanner.nextLine();
	
	                try {
	                    Date fechaInicio = Date.valueOf(fechaInicioStr);
	                    Date fechaFin = Date.valueOf(fechaFinStr);
	                    insertarActividad(conn, idEntrenador, nombre, descripcion, fechaInicio, fechaFin);
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
	                scanner.nextLine(); // Consumir el salto de línea
	
	                System.out.print("Nuevo Nombre de la Actividad: ");
	                String nuevoNombre = scanner.nextLine();

	                System.out.print("Nueva Descripción de la Actividad: ");
	                String nuevaDescripcion = scanner.nextLine();

	                System.out.print("Nueva Fecha de Inicio (yyyy-MM-dd): ");
	                String nuevaFechaInicioStr = scanner.nextLine();
	                System.out.print("Nueva Fecha de Fin (yyyy-MM-dd): ");
	                String nuevaFechaFinStr = scanner.nextLine();
	
	                try {
	                    Date nuevaFechaInicio = Date.valueOf(nuevaFechaInicioStr);
	                    Date nuevaFechaFin = Date.valueOf(nuevaFechaFinStr);
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
		
}
    
