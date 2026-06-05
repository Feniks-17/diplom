import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public class ReservationManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Sklad";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Vvedensky2004";

    private Connection connection;

    public ReservationManager() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static class ReservationInfo {
        private int productId;
        private String sku;
        private String productName;
        private String unit;
        private int warehouseId;
        private String warehouseName;
        private BigDecimal quantity;
        private BigDecimal reservedQuantity;
        private BigDecimal availableQuantity;
        private BigDecimal requestQuantity;

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getReservedQuantity() { return reservedQuantity; }
        public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }
        public BigDecimal getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }
        public BigDecimal getRequestQuantity() { return requestQuantity; }
        public void setRequestQuantity(BigDecimal requestQuantity) { this.requestQuantity = requestQuantity; }

        public void display() {
            System.out.printf("│ %-5d │ %-10s │ %-25s │ %-12s │ %-8s │ %-8s │ %-8s │%n",
                    productId,
                    sku,
                    productName.length() > 25 ? productName.substring(0, 22) + "..." : productName,
                    warehouseName.length() > 12 ? warehouseName.substring(0, 9) + "..." : warehouseName,
                    quantity + " " + unit,
                    reservedQuantity + " " + unit,
                    availableQuantity + " " + unit);
        }
    }

    public static class ReservedItem {
        private int productId;
        private String sku;
        private String productName;
        private String unit;
        private int warehouseId;
        private String warehouseName;
        private BigDecimal reservedQuantity;

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public BigDecimal getReservedQuantity() { return reservedQuantity; }
        public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }

        public void display() {
            System.out.printf("│ %-5d │ %-10s │ %-25s │ %-12s │ %-8s │%n",
                    productId,
                    sku,
                    productName.length() > 25 ? productName.substring(0, 22) + "..." : productName,
                    warehouseName.length() > 12 ? warehouseName.substring(0, 9) + "..." : warehouseName,
                    reservedQuantity + " " + unit);
        }
    }

    public static class WarehouseInfo {
        private int id;
        private String name;
        private String location;
        private boolean isActive;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }

        public void display() {
            System.out.printf("│ %-3d │ %-25s │ %-30s │ %-8s │%n",
                    id,
                    name.length() > 25 ? name.substring(0, 22) + "..." : name,
                    location != null ? (location.length() > 30 ? location.substring(0, 27) + "..." : location) : "-",
                    isActive ? "Активен" : "Неактивен");
        }
    }

    public boolean reserveProduct(int warehouseId, int productId, BigDecimal quantity, String orderNumber) throws SQLException {
        if (!isWarehouseActive(warehouseId)) {
            System.err.println("\n❌ Невозможно резервировать: склад неактивен");
            return false;
        }

        BigDecimal available = getAvailableQuantity(warehouseId, productId);
        if (available.compareTo(quantity) < 0) {
            System.err.println("\n❌ Недостаточно товара для резервирования!");
            System.out.println("   Доступно: " + available + " шт.");
            System.out.println("   Запрошено: " + quantity + " шт.");
            return false;
        }

        connection.setAutoCommit(false);

        try {
            String sql = "UPDATE inventory SET reserved_quantity = reserved_quantity + ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE warehouse_id = ? AND product_id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setBigDecimal(1, quantity);
                pstmt.setInt(2, warehouseId);
                pstmt.setInt(3, productId);
                pstmt.executeUpdate();
            }

            connection.commit();
            System.out.println("\n✅ Товар успешно зарезервирован!");
            System.out.println("   Заказ: " + orderNumber);
            System.out.println("   Количество: " + quantity + " шт.");
            return true;

        } catch (SQLException e) {
            connection.rollback();
            System.err.println("\n❌ Ошибка при резервировании: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public boolean releaseReservation(int warehouseId, int productId, BigDecimal quantity, String orderNumber) throws SQLException {
        BigDecimal reserved = getReservedQuantity(warehouseId, productId);
        if (reserved.compareTo(quantity) < 0) {
            System.err.println("\n❌ Невозможно снять резерв: зарезервировано меньше " + quantity);
            System.out.println("   Зарезервировано: " + reserved + " шт.");
            return false;
        }

        connection.setAutoCommit(false);

        try {
            String sql = "UPDATE inventory SET reserved_quantity = reserved_quantity - ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE warehouse_id = ? AND product_id = ? AND reserved_quantity >= ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setBigDecimal(1, quantity);
                pstmt.setInt(2, warehouseId);
                pstmt.setInt(3, productId);
                pstmt.setBigDecimal(4, quantity);
                int updated = pstmt.executeUpdate();

                if (updated == 0) {
                    throw new SQLException("Не удалось снять резерв");
                }
            }

            connection.commit();
            System.out.println("\n✅ Резерв успешно снят!");
            System.out.println("   Заказ: " + orderNumber);
            System.out.println("   Количество: " + quantity + " шт.");
            return true;

        } catch (SQLException e) {
            connection.rollback();
            System.err.println("\n❌ Ошибка при снятии резерва: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public List<ReservedItem> getReservedItems(int warehouseId) throws SQLException {
        List<ReservedItem> items = new ArrayList<>();
        String sql = "SELECT i.product_id, p.sku, p.name as product_name, p.unit, " +
                "i.reserved_quantity, w.id as warehouse_id, w.name as warehouse_name " +
                "FROM inventory i " +
                "JOIN products p ON i.product_id = p.id " +
                "JOIN warehouses w ON i.warehouse_id = w.id " +
                "WHERE i.warehouse_id = ? AND i.reserved_quantity > 0 " +
                "ORDER BY p.name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ReservedItem item = new ReservedItem();
                    item.setProductId(rs.getInt("product_id"));
                    item.setSku(rs.getString("sku"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnit(rs.getString("unit"));
                    item.setWarehouseId(rs.getInt("warehouse_id"));
                    item.setWarehouseName(rs.getString("warehouse_name"));
                    item.setReservedQuantity(rs.getBigDecimal("reserved_quantity"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public List<ReservedItem> getAllReservedItems() throws SQLException {
        List<ReservedItem> items = new ArrayList<>();
        String sql = "SELECT i.product_id, p.sku, p.name as product_name, p.unit, " +
                "i.reserved_quantity, w.id as warehouse_id, w.name as warehouse_name " +
                "FROM inventory i " +
                "JOIN products p ON i.product_id = p.id " +
                "JOIN warehouses w ON i.warehouse_id = w.id " +
                "WHERE i.reserved_quantity > 0 " +
                "ORDER BY w.name, p.name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ReservedItem item = new ReservedItem();
                item.setProductId(rs.getInt("product_id"));
                item.setSku(rs.getString("sku"));
                item.setProductName(rs.getString("product_name"));
                item.setUnit(rs.getString("unit"));
                item.setWarehouseId(rs.getInt("warehouse_id"));
                item.setWarehouseName(rs.getString("warehouse_name"));
                item.setReservedQuantity(rs.getBigDecimal("reserved_quantity"));
                items.add(item);
            }
        }
        return items;
    }

    public ReservationInfo getProductReservationInfo(int warehouseId, int productId) throws SQLException {
        String sql = "SELECT p.id as product_id, p.sku, p.name as product_name, p.unit, " +
                "w.id as warehouse_id, w.name as warehouse_name, " +
                "COALESCE(i.quantity, 0) as quantity, " +
                "COALESCE(i.reserved_quantity, 0) as reserved_quantity, " +
                "COALESCE(i.quantity - i.reserved_quantity, 0) as available_quantity " +
                "FROM products p " +
                "CROSS JOIN warehouses w " +
                "LEFT JOIN inventory i ON p.id = i.product_id AND w.id = i.warehouse_id " +
                "WHERE w.id = ? AND p.id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            pstmt.setInt(2, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ReservationInfo info = new ReservationInfo();
                    info.setProductId(rs.getInt("product_id"));
                    info.setSku(rs.getString("sku"));
                    info.setProductName(rs.getString("product_name"));
                    info.setUnit(rs.getString("unit"));
                    info.setWarehouseId(rs.getInt("warehouse_id"));
                    info.setWarehouseName(rs.getString("warehouse_name"));
                    info.setQuantity(rs.getBigDecimal("quantity"));
                    info.setReservedQuantity(rs.getBigDecimal("reserved_quantity"));
                    info.setAvailableQuantity(rs.getBigDecimal("available_quantity"));
                    return info;
                }
            }
        }
        return null;
    }

    public List<ReservationInfo> getAvailableProductsForReservation(int warehouseId) throws SQLException {
        List<ReservationInfo> products = new ArrayList<>();
        String sql = "SELECT p.id as product_id, p.sku, p.name as product_name, p.unit, " +
                "w.id as warehouse_id, w.name as warehouse_name, " +
                "COALESCE(i.quantity, 0) as quantity, " +
                "COALESCE(i.reserved_quantity, 0) as reserved_quantity, " +
                "COALESCE(i.quantity - i.reserved_quantity, 0) as available_quantity " +
                "FROM products p " +
                "CROSS JOIN warehouses w " +
                "LEFT JOIN inventory i ON p.id = i.product_id AND w.id = i.warehouse_id " +
                "WHERE w.id = ? AND COALESCE(i.quantity - i.reserved_quantity, 0) > 0 " +
                "ORDER BY p.name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ReservationInfo info = new ReservationInfo();
                    info.setProductId(rs.getInt("product_id"));
                    info.setSku(rs.getString("sku"));
                    info.setProductName(rs.getString("product_name"));
                    info.setUnit(rs.getString("unit"));
                    info.setWarehouseId(rs.getInt("warehouse_id"));
                    info.setWarehouseName(rs.getString("warehouse_name"));
                    info.setQuantity(rs.getBigDecimal("quantity"));
                    info.setReservedQuantity(rs.getBigDecimal("reserved_quantity"));
                    info.setAvailableQuantity(rs.getBigDecimal("available_quantity"));
                    products.add(info);
                }
            }
        }
        return products;
    }

    public List<WarehouseInfo> getAllWarehouses() throws SQLException {
        List<WarehouseInfo> warehouses = new ArrayList<>();
        String sql = "SELECT id, name, location, is_active FROM warehouses ORDER BY name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                WarehouseInfo warehouse = new WarehouseInfo();
                warehouse.setId(rs.getInt("id"));
                warehouse.setName(rs.getString("name"));
                warehouse.setLocation(rs.getString("location"));
                warehouse.setActive(rs.getBoolean("is_active"));
                warehouses.add(warehouse);
            }
        }
        return warehouses;
    }

    private boolean isWarehouseActive(int warehouseId) throws SQLException {
        String sql = "SELECT is_active FROM warehouses WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_active");
                }
            }
        }
        return false;
    }

    private BigDecimal getAvailableQuantity(int warehouseId, int productId) throws SQLException {
        String sql = "SELECT COALESCE(quantity - reserved_quantity, 0) as available " +
                "FROM inventory WHERE warehouse_id = ? AND product_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            pstmt.setInt(2, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("available");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getReservedQuantity(int warehouseId, int productId) throws SQLException {
        String sql = "SELECT COALESCE(reserved_quantity, 0) as reserved " +
                "FROM inventory WHERE warehouse_id = ? AND product_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            pstmt.setInt(2, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("reserved");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Соединение с БД закрыто");
        }
    }

    private static int safeNextInt(Scanner scanner) {
        while (true) {
            try {
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.print("❌ Ошибка! Введите целое число: ");
                scanner.nextLine();
            }
        }
    }

    private static BigDecimal safeNextBigDecimal(Scanner scanner) {
        while (true) {
            try {
                BigDecimal value = scanner.nextBigDecimal();
                scanner.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.print("❌ Ошибка! Введите число: ");
                scanner.nextLine();
            }
        }
    }

    private void showMenu() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║           РЕЗЕРВИРОВАНИЕ ТОВАРОВ           ║");
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.println("║  1. Зарезервировать товар                  ║");
        System.out.println("║  2. Снять резерв с товара                  ║");
        System.out.println("║  3. Просмотреть резерв по складу           ║");
        System.out.println("║  4. Просмотреть весь резерв (все склады)   ║");
        System.out.println("║  5. Проверить доступность товара           ║");
        System.out.println("║  0. Вернуться к выбору модуля              ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.print("➜ Выберите действие: ");
    }

    public void start(Scanner scanner) {
        boolean running = true;

        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║       СИСТЕМА УПРАВЛЕНИЯ СКЛАДОМ           ║");
        System.out.println("║       Модуль: Резервирование товаров       ║");
        System.out.println("╚════════════════════════════════════════════╝");

        while (running) {
            showMenu();
            int choice = safeNextInt(scanner);

            switch (choice) {
                case 1:
                    System.out.println("\n--- РЕЗЕРВИРОВАНИЕ ТОВАРА ---");

                    System.out.println("\nДОСТУПНЫЕ СКЛАДЫ:");
                    System.out.println("┌─────┬───────────────────────────┬──────────────────────────────┬──────────┐");
                    System.out.println("│ ID  │ Название                  │ Адрес                        │ Статус   │");
                    System.out.println("├─────┼───────────────────────────┼──────────────────────────────┼──────────┤");
                    try {
                        List<WarehouseInfo> warehouses = getAllWarehouses();
                        for (WarehouseInfo w : warehouses) {
                            w.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    System.out.println("└─────┴───────────────────────────┴──────────────────────────────┴──────────┘");

                    System.out.print("\nВведите ID склада: ");
                    int warehouseId = safeNextInt(scanner);

                    System.out.println("\nДОСТУПНЫЕ ТОВАРЫ ДЛЯ РЕЗЕРВИРОВАНИЯ:");
                    try {
                        List<ReservationInfo> products = getAvailableProductsForReservation(warehouseId);
                        if (products.isEmpty()) {
                            System.out.println("\n❌ На складе нет доступных товаров для резервирования");
                            break;
                        }

                        System.out.println("┌───────┬────────────┬───────────────────────────┬──────────────┬──────────┬──────────┬──────────┐");
                        System.out.println("│ ID    │ Артикул    │ Наименование              │ Склад        │ Остаток  │ Резерв   │ Доступно │");
                        System.out.println("├───────┼────────────┼───────────────────────────┼──────────────┼──────────┼──────────┼──────────┤");
                        for (ReservationInfo p : products) {
                            p.display();
                        }
                        System.out.println("└───────┴────────────┴───────────────────────────┴──────────────┴──────────┴──────────┴──────────┘");

                        System.out.print("\nВведите ID товара: ");
                        int productId = safeNextInt(scanner);

                        ReservationInfo selected = getProductReservationInfo(warehouseId, productId);
                        if (selected == null) {
                            System.out.println("\n❌ Товар не найден");
                            break;
                        }

                        System.out.println("\nВыбранный товар:");
                        System.out.println("   Наименование: " + selected.getProductName());
                        System.out.println("   Доступно к резерву: " + selected.getAvailableQuantity() + " " + selected.getUnit());

                        System.out.print("\nВведите количество для резервирования: ");
                        BigDecimal quantity = safeNextBigDecimal(scanner);

                        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                            System.out.println("\n❌ Количество должно быть больше 0");
                            break;
                        }

                        System.out.print("Введите номер заказа: ");
                        String orderNumber = scanner.nextLine().trim();
                        if (orderNumber.isEmpty()) {
                            orderNumber = "ORDER_" + System.currentTimeMillis();
                        }

                        reserveProduct(warehouseId, productId, quantity, orderNumber);
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 2:
                    System.out.println("\n--- СНЯТИЕ РЕЗЕРВА С ТОВАРА ---");

                    System.out.println("\nДОСТУПНЫЕ СКЛАДЫ:");
                    System.out.println("┌─────┬───────────────────────────┬──────────────────────────────┬──────────┐");
                    System.out.println("│ ID  │ Название                  │ Адрес                        │ Статус   │");
                    System.out.println("├─────┼───────────────────────────┼──────────────────────────────┼──────────┤");
                    try {
                        List<WarehouseInfo> allWarehouses = getAllWarehouses();
                        for (WarehouseInfo w : allWarehouses) {
                            w.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    System.out.println("└─────┴───────────────────────────┴──────────────────────────────┴──────────┘");

                    System.out.print("\nВведите ID склада: ");
                    int releaseWarehouseId = safeNextInt(scanner);

                    System.out.println("\nЗАРЕЗЕРВИРОВАННЫЕ ТОВАРЫ НА СКЛАДЕ:");
                    try {
                        List<ReservedItem> reservedItems = getReservedItems(releaseWarehouseId);
                        if (reservedItems.isEmpty()) {
                            System.out.println("\n❌ На складе нет зарезервированных товаров");
                            break;
                        }

                        System.out.println("┌───────┬────────────┬───────────────────────────┬──────────────┬──────────┐");
                        System.out.println("│ ID    │ Артикул    │ Наименование              │ Склад        │ Резерв   │");
                        System.out.println("├───────┼────────────┼───────────────────────────┼──────────────┼──────────┤");
                        for (ReservedItem item : reservedItems) {
                            item.display();
                        }
                        System.out.println("└───────┴────────────┴───────────────────────────┴──────────────┴──────────┘");

                        System.out.print("\nВведите ID товара: ");
                        int releaseProductId = safeNextInt(scanner);

                        ReservationInfo releaseInfo = getProductReservationInfo(releaseWarehouseId, releaseProductId);
                        if (releaseInfo == null) {
                            System.out.println("\n❌ Товар не найден");
                            break;
                        }

                        System.out.println("\nВыбранный товар:");
                        System.out.println("   Наименование: " + releaseInfo.getProductName());
                        System.out.println("   Зарезервировано: " + releaseInfo.getReservedQuantity() + " " + releaseInfo.getUnit());

                        System.out.print("\nВведите количество для снятия резерва: ");
                        BigDecimal releaseQuantity = safeNextBigDecimal(scanner);

                        if (releaseQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                            System.out.println("\n❌ Количество должно быть больше 0");
                            break;
                        }

                        System.out.print("Введите номер заказа: ");
                        String releaseOrderNumber = scanner.nextLine().trim();
                        if (releaseOrderNumber.isEmpty()) {
                            releaseOrderNumber = "RELEASE_" + System.currentTimeMillis();
                        }

                        releaseReservation(releaseWarehouseId, releaseProductId, releaseQuantity, releaseOrderNumber);
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 3:
                    System.out.println("\n--- ПРОСМОТР РЕЗЕРВА ПО СКЛАДУ ---");

                    System.out.println("\nДОСТУПНЫЕ СКЛАДЫ:");
                    System.out.println("┌─────┬───────────────────────────┬──────────────────────────────┬──────────┐");
                    System.out.println("│ ID  │ Название                  │ Адрес                        │ Статус   │");
                    System.out.println("├─────┼───────────────────────────┼──────────────────────────────┼──────────┤");
                    try {
                        List<WarehouseInfo> viewWarehouses = getAllWarehouses();
                        for (WarehouseInfo w : viewWarehouses) {
                            w.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    System.out.println("└─────┴───────────────────────────┴──────────────────────────────┴──────────┘");

                    System.out.print("\nВведите ID склада: ");
                    int viewWarehouseId = safeNextInt(scanner);

                    try {
                        List<ReservedItem> items = getReservedItems(viewWarehouseId);
                        if (items.isEmpty()) {
                            System.out.println("\n❌ На складе нет зарезервированных товаров");
                        } else {
                            System.out.println("\n┌───────┬────────────┬───────────────────────────┬──────────────┬──────────┐");
                            System.out.println("│ ID    │ Артикул    │ Наименование              │ Склад        │ Резерв   │");
                            System.out.println("├───────┼────────────┼───────────────────────────┼──────────────┼──────────┤");
                            for (ReservedItem item : items) {
                                item.display();
                            }
                            System.out.println("└───────┴────────────┴───────────────────────────┴──────────────┴──────────┘");

                            BigDecimal totalReserved = items.stream()
                                    .map(ReservedItem::getReservedQuantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            System.out.println("\n📊 ИТОГО ЗАРЕЗЕРВИРОВАНО: " + totalReserved + " ед.");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 4:
                    System.out.println("\n--- ВЕСЬ РЕЗЕРВ (ВСЕ СКЛАДЫ) ---");

                    try {
                        List<ReservedItem> allItems = getAllReservedItems();
                        if (allItems.isEmpty()) {
                            System.out.println("\n❌ Нет зарезервированных товаров");
                        } else {
                            System.out.println("\n┌───────┬────────────┬───────────────────────────┬──────────────┬──────────┐");
                            System.out.println("│ ID    │ Артикул    │ Наименование              │ Склад        │ Резерв   │");
                            System.out.println("├───────┼────────────┼───────────────────────────┼──────────────┼──────────┤");
                            for (ReservedItem item : allItems) {
                                item.display();
                            }
                            System.out.println("└───────┴────────────┴───────────────────────────┴──────────────┴──────────┘");

                            BigDecimal totalAllReserved = allItems.stream()
                                    .map(ReservedItem::getReservedQuantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            System.out.println("\n📊 ВСЕГО ЗАРЕЗЕРВИРОВАНО: " + totalAllReserved + " ед.");
                            System.out.println("📊 Количество позиций: " + allItems.size());
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("\n--- ПРОВЕРКА ДОСТУПНОСТИ ТОВАРА ---");

                    System.out.println("\nДОСТУПНЫЕ СКЛАДЫ:");
                    System.out.println("┌─────┬───────────────────────────┬──────────────────────────────┬──────────┐");
                    System.out.println("│ ID  │ Название                  │ Адрес                        │ Статус   │");
                    System.out.println("├─────┼───────────────────────────┼──────────────────────────────┼──────────┤");
                    try {
                        List<WarehouseInfo> checkWarehouses = getAllWarehouses();
                        for (WarehouseInfo w : checkWarehouses) {
                            w.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    System.out.println("└─────┴───────────────────────────┴──────────────────────────────┴──────────┘");

                    System.out.print("\nВведите ID склада: ");
                    int checkWarehouseId = safeNextInt(scanner);

                    System.out.print("Введите ID товара: ");
                    int checkProductId = safeNextInt(scanner);

                    try {
                        ReservationInfo checkInfo = getProductReservationInfo(checkWarehouseId, checkProductId);
                        if (checkInfo == null) {
                            System.out.println("\n❌ Товар не найден");
                        } else {
                            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
                            System.out.println("║              ИНФОРМАЦИЯ О ДОСТУПНОСТИ                   ║");
                            System.out.println("╠════════════════════════════════════════════════════════════╣");
                            System.out.println("║ Товар: " + checkInfo.getProductName());
                            System.out.println("║ Артикул: " + checkInfo.getSku());
                            System.out.println("║ Склад: " + checkInfo.getWarehouseName());
                            System.out.println("╠════════════════════════════════════════════════════════════╣");
                            System.out.println("║ Общий остаток: " + checkInfo.getQuantity() + " " + checkInfo.getUnit());
                            System.out.println("║ Зарезервировано: " + checkInfo.getReservedQuantity() + " " + checkInfo.getUnit());
                            System.out.println("║ Доступно к резерву: " + checkInfo.getAvailableQuantity() + " " + checkInfo.getUnit());
                            System.out.println("╚════════════════════════════════════════════════════════════╝");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 0:
                    System.out.println("\n╔════════════════════════════════════════════╗");
                    System.out.println("║         Возврат в главное меню...          ║");
                    System.out.println("╚════════════════════════════════════════════╝");
                    running = false;
                    break;

                default:
                    System.out.println("\n❌ Неверный выбор! Пожалуйста, выберите действие от 0 до 5");
                    break;
            }

            if (running && choice != 0) {
                System.out.println("\n────────────────────────────────────────────");
                System.out.print("Нажмите Enter, чтобы продолжить...");
                scanner.nextLine();
            }
        }
    }
}