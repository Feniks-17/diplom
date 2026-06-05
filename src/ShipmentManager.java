import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.util.Date;

public class ShipmentManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Sklad";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Vvedensky2004";

    private Connection connection;

    public ShipmentManager() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static class Shipment {
        private int id;
        private String shipmentNumber;
        private int warehouseId;
        private String warehouseName;
        private Date shipmentDate;
        private String customerName;
        private String status;
        private Timestamp createdAt;
        private List<ShipmentItem> items;

        public Shipment() {
            this.items = new ArrayList<>();
            this.status = "draft";
            this.shipmentDate = new Date(System.currentTimeMillis());
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getShipmentNumber() { return shipmentNumber; }
        public void setShipmentNumber(String shipmentNumber) { this.shipmentNumber = shipmentNumber; }
        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public Date getShipmentDate() { return shipmentDate; }
        public void setShipmentDate(Date shipmentDate) { this.shipmentDate = shipmentDate; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
        public List<ShipmentItem> getItems() { return items; }
        public void setItems(List<ShipmentItem> items) { this.items = items; }
        public void addItem(ShipmentItem item) { this.items.add(item); }

        public BigDecimal getTotalQuantity() {
            return items.stream()
                    .map(ShipmentItem::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public void display() {
            System.out.println("┌─────────────────────────────────────────────────");
            System.out.println("│ РАСХОДНАЯ НАКЛАДНАЯ");
            System.out.println("│ Номер: " + shipmentNumber);
            System.out.println("│ ID: " + id);
            System.out.println("│ Дата: " + shipmentDate);
            System.out.println("│ Склад: " + warehouseName);
            System.out.println("│ Покупатель: " + (customerName != null ? customerName : "-"));
            System.out.println("│ Статус: " + getStatusText());
            System.out.println("│ Создана: " + createdAt);
            System.out.println("│");
            System.out.println("│ ТОВАРЫ К ОТГРУЗКЕ:");
            System.out.println("│ ┌────┬──────────────────────┬──────────┬────────────────────┐");
            System.out.println("│ │ №  │ Наименование         │ Кол-во   │ Доступно на складе │");
            System.out.println("│ ├────┼──────────────────────┼──────────┼────────────────────┤");
            for (int i = 0; i < items.size(); i++) {
                ShipmentItem item = items.get(i);
                System.out.printf("│ │ %-2d │ %-20s │ %-8s │ %-18s │%n",
                        i + 1,
                        item.getProductName().length() > 20 ? item.getProductName().substring(0, 17) + "..." : item.getProductName(),
                        item.getQuantity() + " " + item.getUnit(),
                        item.getAvailableQuantity() + " " + item.getUnit());
            }
            System.out.println("│ └────┴──────────────────────┴──────────┴────────────────────┘");
            System.out.println("│");
            System.out.println("│ ИТОГО КОЛИЧЕСТВО: " + getTotalQuantity() + " ед.");
            System.out.println("└─────────────────────────────────────────────────");
        }

        private String getStatusText() {
            switch (status) {
                case "draft": return "📄 Черновик";
                case "posted": return "✅ Проведена";
                case "cancelled": return "❌ Отменена";
                default: return status;
            }
        }
    }

    public static class ShipmentItem {
        private int id;
        private int shipmentId;
        private int productId;
        private String productName;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal availableQuantity;

        public ShipmentItem() {}

        public ShipmentItem(int productId, BigDecimal quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getShipmentId() { return shipmentId; }
        public void setShipmentId(int shipmentId) { this.shipmentId = shipmentId; }
        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }
    }

    public static class ProductStockInfo {
        private int id;
        private String sku;
        private String name;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal reservedQuantity;
        private BigDecimal availableQuantity;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getReservedQuantity() { return reservedQuantity; }
        public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }
        public BigDecimal getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }

        public void display() {
            System.out.printf("│ %-5d │ %-10s │ %-30s │ %-8s │%n",
                    id, sku,
                    name.length() > 30 ? name.substring(0, 27) + "..." : name,
                    availableQuantity + " " + unit);
        }
    }

    public static class WarehouseInfo {
        private int id;
        private String name;
        private String location;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public void display() {
            System.out.printf("│ %-3d │ %-25s │ %-30s │%n", id,
                    name.length() > 25 ? name.substring(0, 22) + "..." : name,
                    location != null ? (location.length() > 30 ? location.substring(0, 27) + "..." : location) : "-");
        }
    }

    public Shipment createShipment(String shipmentNumber, int warehouseId, Date shipmentDate, String customerName) throws SQLException {
        String sql = "INSERT INTO shipments (shipment_number, warehouse_id, shipment_date, customer_name, status) VALUES (?, ?, ?, ?, 'draft') RETURNING id, created_at";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, shipmentNumber);
            pstmt.setInt(2, warehouseId);
            pstmt.setDate(3, new java.sql.Date(shipmentDate.getTime()));
            pstmt.setString(4, customerName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Shipment shipment = new Shipment();
                    shipment.setId(rs.getInt("id"));
                    shipment.setShipmentNumber(shipmentNumber);
                    shipment.setWarehouseId(warehouseId);
                    shipment.setShipmentDate(shipmentDate);
                    shipment.setCustomerName(customerName);
                    shipment.setStatus("draft");
                    shipment.setCreatedAt(rs.getTimestamp("created_at"));

                    loadShipmentNames(shipment);

                    System.out.println("\n✓ Расходная накладная создана с ID: " + shipment.getId());
                    return shipment;
                }
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.err.println("\n❌ Ошибка: Накладная с номером '" + shipmentNumber + "' уже существует");
            } else {
                throw e;
            }
        }
        return null;
    }

    public boolean addShipmentItem(int shipmentId, int productId, BigDecimal quantity) throws SQLException {
        if (!isShipmentDraft(shipmentId)) {
            System.err.println("\n❌ Невозможно добавить товар: накладная не в статусе 'Черновик'");
            return false;
        }

        Shipment shipment = getShipmentById(shipmentId);
        if (shipment == null) {
            System.err.println("\n❌ Накладная не найдена");
            return false;
        }

        BigDecimal available = getAvailableQuantity(shipment.getWarehouseId(), productId);
        if (available.compareTo(quantity) < 0) {
            System.err.println("\n❌ Недостаточно товара на складе!");
            System.out.println("   Доступно: " + available + " шт.");
            System.out.println("   Запрошено: " + quantity + " шт.");
            return false;
        }

        String sql = "INSERT INTO shipment_items (shipment_id, product_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, shipmentId);
            pstmt.setInt(2, productId);
            pstmt.setBigDecimal(3, quantity);

            pstmt.executeUpdate();
            System.out.println("\n✓ Товар добавлен в накладную");
            return true;
        } catch (SQLException e) {
            System.err.println("\n❌ Ошибка при добавлении товара: " + e.getMessage());
            return false;
        }
    }

    public Shipment getShipmentById(int id) throws SQLException {
        String sql = "SELECT s.*, w.name as warehouse_name " +
                "FROM shipments s " +
                "LEFT JOIN warehouses w ON s.warehouse_id = w.id " +
                "WHERE s.id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Shipment shipment = new Shipment();
                    shipment.setId(rs.getInt("id"));
                    shipment.setShipmentNumber(rs.getString("shipment_number"));
                    shipment.setWarehouseId(rs.getInt("warehouse_id"));
                    shipment.setWarehouseName(rs.getString("warehouse_name"));
                    shipment.setShipmentDate(rs.getDate("shipment_date"));
                    shipment.setCustomerName(rs.getString("customer_name"));
                    shipment.setStatus(rs.getString("status"));
                    shipment.setCreatedAt(rs.getTimestamp("created_at"));

                    loadShipmentItems(shipment);

                    return shipment;
                }
            }
        }
        return null;
    }

    public List<Shipment> getAllShipments() throws SQLException {
        List<Shipment> shipments = new ArrayList<>();
        String sql = "SELECT s.*, w.name as warehouse_name " +
                "FROM shipments s " +
                "LEFT JOIN warehouses w ON s.warehouse_id = w.id " +
                "ORDER BY s.id DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Shipment shipment = new Shipment();
                shipment.setId(rs.getInt("id"));
                shipment.setShipmentNumber(rs.getString("shipment_number"));
                shipment.setWarehouseId(rs.getInt("warehouse_id"));
                shipment.setWarehouseName(rs.getString("warehouse_name"));
                shipment.setShipmentDate(rs.getDate("shipment_date"));
                shipment.setCustomerName(rs.getString("customer_name"));
                shipment.setStatus(rs.getString("status"));
                shipment.setCreatedAt(rs.getTimestamp("created_at"));

                shipments.add(shipment);
            }
        }
        return shipments;
    }

    public boolean postShipment(int shipmentId) throws SQLException {
        if (!isShipmentDraft(shipmentId)) {
            System.err.println("\n❌ Невозможно провести: накладная не в статусе 'Черновик'");
            return false;
        }

        Shipment shipment = getShipmentById(shipmentId);
        if (shipment == null) {
            System.err.println("\n❌ Накладная не найдена");
            return false;
        }

        if (shipment.getItems().isEmpty()) {
            System.err.println("\n❌ Невозможно провести: накладная не содержит товаров");
            return false;
        }

        if (!isWarehouseActive(shipment.getWarehouseId())) {
            System.err.println("\n❌ Невозможно провести: склад неактивен");
            return false;
        }

        for (ShipmentItem item : shipment.getItems()) {
            BigDecimal available = getAvailableQuantity(shipment.getWarehouseId(), item.getProductId());
            if (available.compareTo(item.getQuantity()) < 0) {
                System.err.println("\n❌ Недостаточно товара на складе для позиции: " + item.getProductName());
                System.out.println("   Доступно: " + available + " " + item.getUnit());
                System.out.println("   Запрошено: " + item.getQuantity() + " " + item.getUnit());
                return false;
            }
        }

        connection.setAutoCommit(false);

        try {
            for (ShipmentItem item : shipment.getItems()) {
                decreaseInventory(shipment.getWarehouseId(), item.getProductId(), item.getQuantity());
            }

            String sql = "UPDATE shipments SET status = 'posted' WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, shipmentId);
                pstmt.executeUpdate();
            }

            connection.commit();
            System.out.println("\n✅ Накладная успешно проведена! Товары списаны со склада.");
            return true;

        } catch (SQLException e) {
            connection.rollback();
            System.err.println("\n❌ Ошибка при проведении накладной: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public boolean cancelShipment(int shipmentId) throws SQLException {
        if (!isShipmentDraft(shipmentId)) {
            System.err.println("\n❌ Невозможно отменить: накладная не в статусе 'Черновик'");
            return false;
        }

        String sql = "UPDATE shipments SET status = 'cancelled' WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, shipmentId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("\n✓ Накладная отменена");
                return true;
            } else {
                System.out.println("\n❌ Накладная не найдена");
                return false;
            }
        }
    }

    public boolean removeShipmentItem(int shipmentId, int itemId) throws SQLException {
        if (!isShipmentDraft(shipmentId)) {
            System.err.println("\n❌ Невозможно удалить позицию: накладная не в статусе 'Черновик'");
            return false;
        }

        String sql = "DELETE FROM shipment_items WHERE id = ? AND shipment_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, shipmentId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("\n✓ Позиция удалена из накладной");
                return true;
            } else {
                System.out.println("\n❌ Позиция не найдена");
                return false;
            }
        }
    }

    public BigDecimal checkAvailability(int warehouseId, int productId) throws SQLException {
        return getAvailableQuantity(warehouseId, productId);
    }

    private boolean isShipmentDraft(int shipmentId) throws SQLException {
        String sql = "SELECT status FROM shipments WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, shipmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return "draft".equals(rs.getString("status"));
                }
            }
        }
        return false;
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
        String sql = "SELECT COALESCE(quantity - reserved_quantity, 0) as available FROM inventory WHERE warehouse_id = ? AND product_id = ?";

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

    private void decreaseInventory(int warehouseId, int productId, BigDecimal quantity) throws SQLException {
        String sql = "UPDATE inventory SET quantity = quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE warehouse_id = ? AND product_id = ? AND quantity >= ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, quantity);
            pstmt.setInt(2, warehouseId);
            pstmt.setInt(3, productId);
            pstmt.setBigDecimal(4, quantity);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Недостаточно товара на складе для списания");
            }
        }
    }

    private void loadShipmentNames(Shipment shipment) throws SQLException {
        String warehouseSql = "SELECT name FROM warehouses WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(warehouseSql)) {
            pstmt.setInt(1, shipment.getWarehouseId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    shipment.setWarehouseName(rs.getString("name"));
                }
            }
        }
    }

    private void loadShipmentItems(Shipment shipment) throws SQLException {
        String sql = "SELECT si.*, p.name as product_name, p.unit, " +
                "COALESCE(i.quantity - i.reserved_quantity, 0) as available_quantity " +
                "FROM shipment_items si " +
                "JOIN products p ON si.product_id = p.id " +
                "LEFT JOIN inventory i ON i.product_id = p.id AND i.warehouse_id = ? " +
                "WHERE si.shipment_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, shipment.getWarehouseId());
            pstmt.setInt(2, shipment.getId());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ShipmentItem item = new ShipmentItem();
                    item.setId(rs.getInt("id"));
                    item.setShipmentId(rs.getInt("shipment_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnit(rs.getString("unit"));
                    item.setQuantity(rs.getBigDecimal("quantity"));
                    item.setAvailableQuantity(rs.getBigDecimal("available_quantity"));

                    shipment.addItem(item);
                }
            }
        }
    }

    public List<ProductStockInfo> getProductsWithStock(int warehouseId) throws SQLException {
        List<ProductStockInfo> products = new ArrayList<>();
        String sql = "SELECT p.id, p.sku, p.name, p.unit, " +
                "COALESCE(i.quantity, 0) as quantity, " +
                "COALESCE(i.reserved_quantity, 0) as reserved_quantity, " +
                "COALESCE(i.quantity - i.reserved_quantity, 0) as available_quantity " +
                "FROM products p " +
                "LEFT JOIN inventory i ON p.id = i.product_id AND i.warehouse_id = ? " +
                "ORDER BY p.name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ProductStockInfo product = new ProductStockInfo();
                    product.setId(rs.getInt("id"));
                    product.setSku(rs.getString("sku"));
                    product.setName(rs.getString("name"));
                    product.setUnit(rs.getString("unit"));
                    product.setQuantity(rs.getBigDecimal("quantity"));
                    product.setReservedQuantity(rs.getBigDecimal("reserved_quantity"));
                    product.setAvailableQuantity(rs.getBigDecimal("available_quantity"));

                    products.add(product);
                }
            }
        }
        return products;
    }

    public List<WarehouseInfo> getActiveWarehouses() throws SQLException {
        List<WarehouseInfo> warehouses = new ArrayList<>();
        String sql = "SELECT id, name, location FROM warehouses WHERE is_active = true ORDER BY name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                WarehouseInfo warehouse = new WarehouseInfo();
                warehouse.setId(rs.getInt("id"));
                warehouse.setName(rs.getString("name"));
                warehouse.setLocation(rs.getString("location"));
                warehouses.add(warehouse);
            }
        }
        return warehouses;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Соединение с БД закрыто");
        }
    }

    private void showMenu() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║           РАСХОД ТОВАРА                    ║");
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.println("║  1. Создать расходную накладную            ║");
        System.out.println("║  2. Добавить товар в накладную             ║");
        System.out.println("║  3. Просмотреть накладную                  ║");
        System.out.println("║  4. Показать все накладные                 ║");
        System.out.println("║  5. Провести накладную                     ║");
        System.out.println("║  6. Отменить накладную                     ║");
        System.out.println("║  7. Удалить позицию из накладной           ║");
        System.out.println("║  8. Проверить остатки товара               ║");
        System.out.println("║  0. Вернуться к выбору модуля              ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.print("➜ Выберите действие: ");
    }

    public void start(Scanner scanner) {
        boolean running = true;
        Integer currentShipmentId = null;

        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║       СИСТЕМА УПРАВЛЕНИЯ СКЛАДОМ           ║");
        System.out.println("║       Модуль: Расход товара                ║");
        System.out.println("╚════════════════════════════════════════════╝");

        while (running) {
            showMenu();
            int choice;

            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("\n❌ Ошибка: Введите число!");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.println("\n--- СОЗДАНИЕ РАСХОДНОЙ НАКЛАДНОЙ ---");
                    System.out.print("Номер накладной: ");
                    String number = scanner.nextLine().trim();
                    if (number.isEmpty()) {
                        System.out.println("❌ Номер не может быть пустым!");
                        break;
                    }

                    System.out.println("\nАКТИВНЫЕ СКЛАДЫ:");
                    System.out.println("┌─────┬───────────────────────────┬──────────────────────────────┐");
                    System.out.println("│ ID  │ Название                  │ Адрес                        │");
                    System.out.println("├─────┼───────────────────────────┼──────────────────────────────┤");
                    try {
                        List<WarehouseInfo> warehouses = getActiveWarehouses();
                        for (WarehouseInfo w : warehouses) {
                            w.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    System.out.println("└─────┴───────────────────────────┴──────────────────────────────┘");

                    System.out.print("\nID склада: ");
                    int warehouseId;
                    try {
                        warehouseId = scanner.nextInt();
                        scanner.nextLine();
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка: Введите корректный ID склада!");
                        scanner.nextLine();
                        break;
                    }

                    System.out.print("Покупатель (наименование): ");
                    String customerName = scanner.nextLine().trim();

                    try {
                        Shipment shipment = createShipment(number, warehouseId, new Date(System.currentTimeMillis()), customerName);
                        if (shipment != null) {
                            currentShipmentId = shipment.getId();
                            shipment.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 2:
                    if (currentShipmentId == null) {
                        System.out.println("\n⚠️  Сначала создайте накладную (пункт 1)");
                        break;
                    }

                    System.out.println("\n--- ДОБАВЛЕНИЕ ТОВАРА В НАКЛАДНУЮ ---");
                    System.out.println("Текущая накладная ID: " + currentShipmentId);

                    try {
                        Shipment currentShipment = getShipmentById(currentShipmentId);
                        if (currentShipment == null) {
                            System.out.println("❌ Накладная не найдена");
                            currentShipmentId = null;
                            break;
                        }

                        System.out.println("\nДОСТУПНЫЕ ТОВАРЫ НА СКЛАДЕ " + currentShipment.getWarehouseName() + ":");
                        System.out.println("┌───────┬────────────┬────────────────────────────────┬──────────┐");
                        System.out.println("│ ID    │ Артикул    │ Название                       │ Доступно │");
                        System.out.println("├───────┼────────────┼────────────────────────────────┼──────────┤");
                        List<ProductStockInfo> products = getProductsWithStock(currentShipment.getWarehouseId());
                        for (ProductStockInfo p : products) {
                            p.display();
                        }
                        System.out.println("└───────┴────────────┴────────────────────────────────┴──────────┘");

                        System.out.print("\nID товара: ");
                        int productId;
                        try {
                            productId = scanner.nextInt();
                            scanner.nextLine();
                        } catch (Exception e) {
                            System.out.println("❌ Ошибка ввода!");
                            scanner.nextLine();
                            break;
                        }

                        System.out.print("Количество к отгрузке: ");
                        BigDecimal quantity;
                        try {
                            quantity = scanner.nextBigDecimal();
                            scanner.nextLine();
                        } catch (Exception e) {
                            System.out.println("❌ Ошибка ввода!");
                            scanner.nextLine();
                            break;
                        }

                        addShipmentItem(currentShipmentId, productId, quantity);
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 3:
                    System.out.println("\n--- ПРОСМОТР НАКЛАДНОЙ ---");
                    System.out.print("Введите ID накладной (Enter - текущая " + currentShipmentId + "): ");
                    String input = scanner.nextLine().trim();
                    int viewId;
                    if (input.isEmpty() && currentShipmentId != null) {
                        viewId = currentShipmentId;
                    } else {
                        try {
                            viewId = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Ошибка ввода!");
                            break;
                        }
                    }

                    try {
                        Shipment viewShipment = getShipmentById(viewId);
                        if (viewShipment != null) {
                            viewShipment.display();
                        } else {
                            System.out.println("\n❌ Накладная не найдена");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 4:
                    System.out.println("\n--- ВСЕ РАСХОДНЫЕ НАКЛАДНЫЕ ---");
                    try {
                        List<Shipment> allShipments = getAllShipments();
                        if (allShipments.isEmpty()) {
                            System.out.println("\n❌ Накладные не найдены");
                        } else {
                            System.out.println("\n✅ Всего накладных: " + allShipments.size());
                            for (Shipment s : allShipments) {
                                System.out.println("\n┌─────────────────────────────────────────────────");
                                System.out.println("│ Накладная #" + s.getId());
                                System.out.println("│ Номер: " + s.getShipmentNumber());
                                System.out.println("│ Дата: " + s.getShipmentDate());
                                System.out.println("│ Склад: " + s.getWarehouseName());
                                System.out.println("│ Покупатель: " + (s.getCustomerName() != null ? s.getCustomerName() : "-"));
                                System.out.println("│ Статус: " + s.getStatus());
                                System.out.println("└─────────────────────────────────────────────────");
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("\n--- ПРОВЕДЕНИЕ НАКЛАДНОЙ ---");
                    System.out.print("Введите ID накладной для проведения: ");
                    try {
                        int postId = scanner.nextInt();
                        scanner.nextLine();
                        postShipment(postId);
                        if (currentShipmentId != null && currentShipmentId == postId) {
                            currentShipmentId = null;
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка ввода!");
                        scanner.nextLine();
                    }
                    break;

                case 6:
                    System.out.println("\n--- ОТМЕНА НАКЛАДНОЙ ---");
                    System.out.print("Введите ID накладной для отмены: ");
                    try {
                        int cancelId = scanner.nextInt();
                        scanner.nextLine();
                        cancelShipment(cancelId);
                        if (currentShipmentId != null && currentShipmentId == cancelId) {
                            currentShipmentId = null;
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка ввода!");
                        scanner.nextLine();
                    }
                    break;

                case 7:
                    if (currentShipmentId == null) {
                        System.out.println("\n⚠️  Нет текущей накладной");
                        break;
                    }

                    System.out.println("\n--- УДАЛЕНИЕ ПОЗИЦИИ ИЗ НАКЛАДНОЙ ---");
                    System.out.println("Текущая накладная ID: " + currentShipmentId);

                    try {
                        Shipment current = getShipmentById(currentShipmentId);
                        if (current != null && !current.getItems().isEmpty()) {
                            System.out.println("\nПозиции в накладной:");
                            for (int i = 0; i < current.getItems().size(); i++) {
                                ShipmentItem item = current.getItems().get(i);
                                System.out.println((i + 1) + ". ID=" + item.getId() + " - " +
                                        item.getProductName() + " - " + item.getQuantity() + " " + item.getUnit());
                            }
                            System.out.print("\nВведите ID позиции для удаления: ");
                            try {
                                int itemId = scanner.nextInt();
                                scanner.nextLine();
                                removeShipmentItem(currentShipmentId, itemId);
                            } catch (Exception e) {
                                System.out.println("❌ Ошибка ввода!");
                                scanner.nextLine();
                            }
                        } else {
                            System.out.println("\n❌ В накладной нет позиций");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 8:
                    System.out.println("\n--- ПРОВЕРКА ОСТАТКОВ ТОВАРА ---");
                    System.out.print("Введите ID склада: ");
                    try {
                        int checkWarehouseId = scanner.nextInt();
                        scanner.nextLine();

                        try {
                            List<ProductStockInfo> stockProducts = getProductsWithStock(checkWarehouseId);
                            if (stockProducts.isEmpty()) {
                                System.out.println("\n❌ На складе нет товаров");
                            } else {
                                System.out.println("\n┌───────┬────────────┬────────────────────────────────┬──────────┬────────────┬──────────┐");
                                System.out.println("│ ID    │ Артикул    │ Название                       │ Остаток  │ Резерв     │ Доступно │");
                                System.out.println("├───────┼────────────┼────────────────────────────────┼──────────┼────────────┼──────────┤");
                                for (ProductStockInfo p : stockProducts) {
                                    System.out.printf("│ %-5d │ %-10s │ %-30s │ %-8s │ %-10s │ %-8s │%n",
                                            p.getId(),
                                            p.getSku(),
                                            p.getName().length() > 30 ? p.getName().substring(0, 27) + "..." : p.getName(),
                                            p.getQuantity() + " " + p.getUnit(),
                                            p.getReservedQuantity() + " " + p.getUnit(),
                                            p.getAvailableQuantity() + " " + p.getUnit());
                                }
                                System.out.println("└───────┴────────────┴────────────────────────────────┴──────────┴────────────┴──────────┘");
                            }
                        } catch (SQLException e) {
                            System.err.println("Ошибка БД: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка ввода!");
                        scanner.nextLine();
                    }
                    break;

                case 0:
                    System.out.println("\n╔════════════════════════════════════════════╗");
                    System.out.println("║         Возврат в главное меню...          ║");
                    System.out.println("╚════════════════════════════════════════════╝");
                    running = false;
                    break;

                default:
                    System.out.println("\n❌ Неверный выбор! Пожалуйста, выберите действие от 0 до 8");
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