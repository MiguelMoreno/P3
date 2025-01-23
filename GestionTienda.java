import java.sql.*;
import java.util.Scanner;

public class GestionTienda {
    
    /***************************************************/
    /* AÑADIR PRODUCTO A LA TIENDA */
    /***************************************************/
    private static void agregarProducto(Connection conn, String nombre, int stock, String proveedor) {
        try {
            if (stock <= 0) {
                System.out.println("Error: El stock debe ser un número positivo.");
                return;
            }

            String query = "INSERT INTO Producto (Nombre, Stock, Proveedor) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, nombre);
                pstmt.setInt(2, stock);
                pstmt.setString(3, proveedor);
                pstmt.executeUpdate();
                conn.commit();
                System.out.println("Producto añadido correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
            rollback(conn);
        }
    }


    /***************************************************/
    /* VER STOCK DE PRODUCTO */
    /***************************************************/
    private static void verStockProducto(Connection conn, int idProducto) {
        String query = "SELECT Nombre, Stock FROM Producto WHERE ID_Producto = ? AND Eliminado = 0";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Producto: " + rs.getString("Nombre") + ", Stock Disponible: " + rs.getInt("Stock"));
            } else {
                System.out.println("Producto no encontrado o eliminado.");
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar stock del producto: " + e.getMessage());
        }
    }

    /***************************************************/
    /* RECIBIR PRODUCTO */
    /***************************************************/
    private static void recibirProducto(Connection conn, int idProducto, int cantidad, String proveedor) {
        try {
            if (cantidad <= 0) {
                System.out.println("Error: La cantidad recibida debe ser un número positivo.");
                return;
            }

            String verificarProveedor = "SELECT COUNT(*) FROM Producto WHERE ID_Producto = ? AND Proveedor = ?";
            try (PreparedStatement stmt = conn.prepareStatement(verificarProveedor)) {
                stmt.setInt(1, idProducto);
                stmt.setString(2, proveedor);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    System.out.println("Error: Proveedor no válido.");
                    return;
                }
            }

            String actualizarStock = "UPDATE Producto SET Stock = Stock + ? WHERE ID_Producto = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(actualizarStock)) {
                pstmt.setInt(1, cantidad);
                pstmt.setInt(2, idProducto);
                pstmt.executeUpdate();
                conn.commit();
                System.out.println("Producto recibido y stock actualizado.");
            }

        } catch (SQLException e) {
            System.err.println("Error al recibir producto: " + e.getMessage());
            rollback(conn);
        }
    }

    /***************************************************/
    /* COMPRAR PRODUCTO */
    /***************************************************/
    private static void comprarProducto(Connection conn, int idProducto, int idCliente, int cantidad, double totalVenta) {
        try {
            // 1. Verificar la existencia del cliente
            String verificarCliente = "SELECT COUNT(*) FROM ClienteIncluyeSuscripcion_Invitacion WHERE ID_Cliente = ?";
            try (PreparedStatement stmtCliente = conn.prepareStatement(verificarCliente)) {
                stmtCliente.setInt(1, idCliente);
                ResultSet rs = stmtCliente.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    System.out.println("Error: El cliente no está registrado en el sistema.");
                    return;
                }
            }
    
            // 2. Verificar disponibilidad del producto
            String verificarStock = "SELECT Stock FROM Producto WHERE ID_Producto = ? AND Eliminado = 0";
            try (PreparedStatement stmtStock = conn.prepareStatement(verificarStock)) {
                stmtStock.setInt(1, idProducto);
                ResultSet rs = stmtStock.executeQuery();
                if (rs.next()) {
                    int stockDisponible = rs.getInt("Stock");
                    if (cantidad > stockDisponible) {
                        System.out.println("Error: No hay suficiente stock disponible.");
                        return;
                    }
                } else {
                    System.out.println("Error: Producto no encontrado.");
                    return;
                }
            }
    
            // 3. Insertar la compra en la tabla Comprar
            String queryCompra = "INSERT INTO Comprar (ID_Producto, ID_Cliente, Cantidad_Venta, Total_Venta, Fecha, Eliminado) VALUES (?, ?, ?, ?, SYSDATE, 0)";
            try (PreparedStatement pstmt = conn.prepareStatement(queryCompra)) {
                pstmt.setInt(1, idProducto);
                pstmt.setInt(2, idCliente);
                pstmt.setInt(3, cantidad);
                pstmt.setDouble(4, totalVenta);
                pstmt.executeUpdate();
            }
    
            // 4. Actualizar el stock del producto
            String actualizarStock = "UPDATE Producto SET Stock = Stock - ? WHERE ID_Producto = ?";
            try (PreparedStatement pstmtStock = conn.prepareStatement(actualizarStock)) {
                pstmtStock.setInt(1, cantidad);
                pstmtStock.setInt(2, idProducto);
                pstmtStock.executeUpdate();
            }
    
            // 5. Confirmar la transacción
            conn.commit();
            System.out.println("Compra realizada con éxito.");
    
        } catch (SQLException e) {
            System.err.println("Error al comprar producto: " + e.getMessage());
            rollback(conn);
        }
   }

    /***************************************************/
    /* AÑADIR PRODUCTO A LISTA DE DESEADOS */
    /***************************************************/
    private static void agregarAListaDeseados(Connection conn, int idProducto, int cantidad, int idCliente) {
        try {
            // Verificar que el cliente existe en la base de datos
            String verificarCliente = "SELECT COUNT(*) FROM ClienteIncluyeSuscripcion_Invitacion WHERE ID_Cliente = ?";
            try (PreparedStatement stmtCliente = conn.prepareStatement(verificarCliente)) {
                stmtCliente.setInt(1, idCliente);
                ResultSet rs = stmtCliente.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    System.out.println("Error: El cliente no está registrado en el sistema.");
                    return;
                }
            }
    
            // Verificar si hay suficiente stock disponible
            String verificarStock = "SELECT Stock FROM Producto WHERE ID_Producto = ? AND Eliminado = 0";
            try (PreparedStatement stmtStock = conn.prepareStatement(verificarStock)) {
                stmtStock.setInt(1, idProducto);
                ResultSet rs = stmtStock.executeQuery();
                if (rs.next()) {
                    int stockDisponible = rs.getInt("Stock");
                    if (cantidad > stockDisponible) {
                        System.out.println("Error: La cantidad solicitada excede el stock disponible.");
                        return;
                    }
                } else {
                    System.out.println("Error: Producto no encontrado.");
                    return;
                }
            }
    
            // Insertar en la tabla de lista de deseados
            String query = "INSERT INTO ListaDeseados (ID_Cliente, ID_Producto, Cantidad, Fecha_Añadido) VALUES (?, ?, ?, SYSDATE)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, idCliente);
                pstmt.setInt(2, idProducto);
                pstmt.setInt(3, cantidad);
                pstmt.executeUpdate();
                conn.commit();
                System.out.println("Producto añadido a la lista de deseados correctamente.");
            }
    
        } catch (SQLException e) {
            System.err.println("Error al añadir producto a lista de deseados: " + e.getMessage());
            rollback(conn);
        }
    }
    
    

    public static void mostrarMenuGestionTienda(Connection conn, Scanner scanner) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\nGestión de Tienda:");
            System.out.println("1. Añadir Producto");
            System.out.println("2. Ver Stock de Producto");
            System.out.println("3. Recibir Producto");
            System.out.println("4. Comprar Producto");
            System.out.println("5. Añadir Producto a Lista de Deseados");
            System.out.println("6. Volver al Menú Principal");
            System.out.print("Elige una opción: ");
    
            int opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir salto de línea
    
            switch (opcion) {
                case 1:
                    System.out.print("Nombre del Producto: ");
                    String nombre = scanner.nextLine();
                    System.out.print("Stock Inicial: ");
                    int stock = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Proveedor: ");
                    String proveedor = scanner.nextLine();
                    agregarProducto(conn, nombre, stock, proveedor);
                    break;
    
                case 2:
                    System.out.print("ID Producto: ");
                    int idProducto = scanner.nextInt();
                    verStockProducto(conn, idProducto);
                    break;
    
                case 3:
                    System.out.print("ID Producto: ");
                    idProducto = scanner.nextInt();
                    System.out.print("Cantidad recibida: ");
                    int cantidad = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Proveedor: ");
                    proveedor = scanner.nextLine();
                    recibirProducto(conn, idProducto, cantidad, proveedor);
                    break;
    
                case 4:
                    System.out.print("ID Producto: ");
                    idProducto = scanner.nextInt();
                    System.out.print("ID Cliente: ");
                    int idCliente = scanner.nextInt();
                    System.out.print("Cantidad: ");
                    cantidad = scanner.nextInt();
                    System.out.print("Total Venta: ");
                    double totalVenta = scanner.nextDouble();
                    comprarProducto(conn, idProducto, idCliente, cantidad, totalVenta);
                    break;
    
                case 5:
                    System.out.print("ID Producto: ");
                    idProducto = scanner.nextInt();
                    System.out.print("Cantidad: ");
                    cantidad = scanner.nextInt();
                    System.out.print("ID Cliente: ");
                    idCliente = scanner.nextInt();
                    agregarAListaDeseados(conn, idProducto, cantidad, idCliente);
                    break;
    
                case 6:
                    salir = true;
                    break;
    
                default:
                    System.out.println("Opción no válida. Intenta de nuevo.");
            }
        }
    }
    
    private static void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            System.err.println("Error al revertir la transacción.");
        }
    }
}
