import java.sql.*;
import java.util.Scanner;

public class SistemaGimnasio {

    private static class DatabaseConnection {
        private static final String URL = "jdbc:oracle:thin:@//oracle0.ugr.es:1521/practbd";
        private static final String USER = "x8124805";
        private static final String PASSWORD = "x8124805";

        public static Connection connect() throws SQLException {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);
            return conn;
        }

        public static void disconnect(Connection conn) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("Conexión cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    private static void mostrarAvisoLegal() {
        System.out.println("\n*** Aviso Legal ***");
        System.out.println("Este sistema almacena datos personales de empleados, clientes y productos.");
        System.out.println("Todos los datos se manejan de acuerdo con la legislación vigente sobre protección de datos.");
        System.out.println("*******************\n");
    }

    private static void crearTablas(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        String[] tablas = {
            "CREATE TABLE Empleado (idEmpleado INT PRIMARY KEY, nombre VARCHAR(100) NOT NULL, rol VARCHAR(50) NOT NULL)",
            "CREATE TABLE Cliente (idCliente INT PRIMARY KEY, nombre VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL UNIQUE)",
            "CREATE TABLE Producto (idProducto INT PRIMARY KEY, nombre VARCHAR(100) NOT NULL, stock INT NOT NULL CHECK (stock >= 0), precio DECIMAL(10, 2) NOT NULL)"
        };

        for (String tabla : tablas) {
            try {
                stmt.executeUpdate(tabla);
            } catch (SQLException e) {
                if (e.getErrorCode() != 955) {
                    throw e;
                }
            }
        }
        stmt.close();
    }

    private static void listarTablas(Connection conn) {
        String query = "SELECT table_name FROM user_tables";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Tablas disponibles en la base de datos:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("table_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar tablas: " + e.getMessage());
        }
    }

    private static void listarDatosDeTabla(Connection conn, String nombreTabla) {
        String query = "SELECT * FROM " + nombreTabla;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            System.out.println("\nDatos de la tabla " + nombreTabla + ":");

            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error al listar datos de la tabla: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.connect();
            mostrarAvisoLegal();
            crearTablas(conn);

            Scanner scanner = new Scanner(System.in);
            boolean salir = false;

            while (!salir) {
                System.out.println("\nMenú Principal:");
                System.out.println("1. Gestión de Empleados");
                System.out.println("2. Gestión de Clientes");
                System.out.println("3. Gestión de Actividades");
                System.out.println("4. Gestión de Tienda");
                System.out.println("5. Listar Tablas");
                System.out.println("6. Salir");
                System.out.print("Elige una opción: ");

                int opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1:
                        // Llamar a la clase GestionEmpleados para gestionar empleados
                        GestionEmpleados.mostrarMenuGestionEmpleados(conn, scanner);
                        break;
                    case 2:
                        System.out.println("Gestión de Clientes no implementada en esta versión.");
                        break;
                    case 3:
                        System.out.println("Gestión de Actividades no implementada en esta versión.");
                        break;
                    case 4:
                        System.out.println("Gestión de Tienda no implementada en esta versión.");
                        break;
                    case 5:
                        listarTablas(conn);
                        System.out.print("Introduce el nombre de la tabla que deseas consultar: ");
                        String nombreTabla = scanner.nextLine();
                        listarDatosDeTabla(conn, nombreTabla);
                        break;
                    case 6:
                        salir = true;
                        System.out.println("Saliendo del sistema...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }

            scanner.close();
        } catch (SQLException e) {
            System.err.println("Error al interactuar con la base de datos: " + e.getMessage());
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }
}
