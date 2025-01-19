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

}
    
