import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public class StockControlManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Sklad";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Vvedensky2004";

    private Connection connection;

    public StockControlManager() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static class StockByWarehouse {
        private int productId;
        private String sku;
        private String productName;
        private String category;
        private String unit;
        private int warehouseId;
        private String warehouseName;
        private BigDecimal quantity;
        private BigDecimal reservedQuantity;
        private BigDecimal availableQuantity;

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
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

    public static class TotalStockByProduct {
        private int productId;
        private String sku;
        private String productName;
        private String category;
        private String unit;
        private BigDecimal totalQuantity;
        private BigDecimal totalReserved;
        private BigDecimal totalAvailable;

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }
        public BigDecimal getTotalReserved() { return totalReserved; }
        public void setTotalReserved(BigDecimal totalReserved) { this.totalReserved = totalReserved; }
        public BigDecimal getTotalAvailable() { return totalAvailable; }
        public void setTotalAvailable(BigDecimal totalAvailable) { this.totalAvailable = totalAvailable; }

        public void display() {
            System.out.printf("│ %-5d │ %-10s │ %-30s │ %-12s │ %-10s │ %-10s │%n",
                    productId,
                    sku,
                    productName.length() > 30 ? productName.substring(0, 27) + "..." : productName,
                    totalQuantity + " " + unit,
                    totalReserved + " " + unit,
                    totalAvailable + " " + unit);
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

    public static class CriticalStock {
        private int productId;
        private String sku;
        private String productName;
        private String unit;
        private int warehouseId;
        private String warehouseName;
        private BigDecimal availableQuantity;
        private BigDecimal criticalLevel;

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
        public BigDecimal getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }
        public BigDecimal getCriticalLevel() { return criticalLevel; }
        public void setCriticalLevel(BigDecimal criticalLevel) { this.criticalLevel = criticalLevel; }

        public void display() {
            System.out.printf("│ %-5d │ %-10s │ %-25s │ %-12s │ %-10s │ %-10s │%n",
                    productId,
                    sku,
                    productName.length() > 25 ? productName.substring(0, 22) + "..." : productName,
                    warehouseName.length() > 12 ? warehouseName.substring(0, 9) + "..." : warehouseName,
                    availableQuantity + " " + unit,
                    "< " + criticalLevel + " " + unit);
        }
    }

    public static class ProductInfo {
        private int id;
        private String sku;
        private String name;
        private String unit;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public void display() {
            System.out.printf("│ %-5d │ %-10s │ %-40s │ %-5s │%n", id, sku,
                    name.length() > 40 ? name.substring(0, 37) + "..." : name, unit);
        }
    }

    public List<StockByWarehouse> getStockByWarehouse(int warehouseId) throws SQLException {
        List<StockByWarehouse> stocks = new ArrayList<>();
        String sql = "SELECT product_id, sku, name, category, warehouse_id, warehouse_name, " +
                "quantity, reserved_quantity, available_quantity " +
                "FROM current_stock WHERE warehouse_id = ? ORDER BY name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StockByWarehouse stock = new StockByWarehouse();
                    stock.setProductId(rs.getInt("product_id"));
                    stock.setSku(rs.getString("sku"));
                    stock.setProductName(rs.getString("name"));
                    stock.setCategory(rs.getString("category"));
                    stock.setWarehouseId(rs.getInt("warehouse_id"));
                    stock.setWarehouseName(rs.getString("warehouse_name"));
                    stock.setQuantity(rs.getBigDecimal("quantity"));
                    stock.setReservedQuantity(rs.getBigDecimal("reserved_quantity"));
                    stock.setAvailableQuantity(rs.getBigDecimal("available_quantity"));

                    String unitSql = "SELECT unit FROM products WHERE id = ?";
                    try (PreparedStatement unitStmt = connection.prepareStatement(unitSql)) {
                        unitStmt.setInt(1, stock.getProductId());
                        try (ResultSet unitRs = unitStmt.executeQuery()) {
                            if (unitRs.next()) {
                                stock.setUnit(unitRs.getString("unit"));
                            } else {
                                stock.setUnit("шт");
                            }
                        }
                    }

                    stocks.add(stock);
                }
            }
        }
        return stocks;
    }

    public TotalStockByProduct getTotalStockByProduct(int productId) throws SQLException {
        String sql = "SELECT product_id, sku, name, category, total_quantity, total_reserved, total_available " +
                "FROM total_stock WHERE product_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    TotalStockByProduct stock = new TotalStockByProduct();
                    stock.setProductId(rs.getInt("product_id"));
                    stock.setSku(rs.getString("sku"));
                    stock.setProductName(rs.getString("name"));
                    stock.setCategory(rs.getString("category"));
                    stock.setTotalQuantity(rs.getBigDecimal("total_quantity"));
                    stock.setTotalReserved(rs.getBigDecimal("total_reserved"));
                    stock.setTotalAvailable(rs.getBigDecimal("total_available"));

                    String unitSql = "SELECT unit FROM products WHERE id = ?";
                    try (PreparedStatement unitStmt = connection.prepareStatement(unitSql)) {
                        unitStmt.setInt(1, stock.getProductId());
                        try (ResultSet unitRs = unitStmt.executeQuery()) {
                            if (unitRs.next()) {
                                stock.setUnit(unitRs.getString("unit"));
                            } else {
                                stock.setUnit("шт");
                            }
                        }
                    }

                    return stock;
                }
            }
        }
        return null;
    }

    public List<TotalStockByProduct> getAllTotalStocks() throws SQLException {
        List<TotalStockByProduct> stocks = new ArrayList<>();
        String sql = "SELECT product_id, sku, name, category, total_quantity, total_reserved, total_available " +
                "FROM total_stock ORDER BY name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TotalStockByProduct stock = new TotalStockByProduct();
                stock.setProductId(rs.getInt("product_id"));
                stock.setSku(rs.getString("sku"));
                stock.setProductName(rs.getString("name"));
                stock.setCategory(rs.getString("category"));
                stock.setTotalQuantity(rs.getBigDecimal("total_quantity"));
                stock.setTotalReserved(rs.getBigDecimal("total_reserved"));
                stock.setTotalAvailable(rs.getBigDecimal("total_available"));

                String unitSql = "SELECT unit FROM products WHERE id = ?";
                try (PreparedStatement unitStmt = connection.prepareStatement(unitSql)) {
                    unitStmt.setInt(1, stock.getProductId());
                    try (ResultSet unitRs = unitStmt.executeQuery()) {
                        if (unitRs.next()) {
                            stock.setUnit(unitRs.getString("unit"));
                        } else {
                            stock.setUnit("шт");
                        }
                    }
                }

                stocks.add(stock);
            }
        }
        return stocks;
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

    public List<ProductInfo> getAllProducts() throws SQLException {
        List<ProductInfo> products = new ArrayList<>();
        String sql = "SELECT id, sku, name, unit FROM products ORDER BY name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ProductInfo product = new ProductInfo();
                product.setId(rs.getInt("id"));
                product.setSku(rs.getString("sku"));
                product.setName(rs.getString("name"));
                product.setUnit(rs.getString("unit"));
                products.add(product);
            }
        }
        return products;
    }

    public List<CriticalStock> findCriticalStocks(BigDecimal criticalLevel) throws SQLException {
        List<CriticalStock> criticalStocks = new ArrayList<>();
        String sql = "SELECT " +
                "p.id as product_id, p.sku, p.name as product_name, p.unit, " +
                "w.id as warehouse_id, w.name as warehouse_name, " +
                "COALESCE(i.quantity - i.reserved_quantity, 0) as available_quantity " +
                "FROM products p " +
                "CROSS JOIN warehouses w " +
                "LEFT JOIN inventory i ON p.id = i.product_id AND w.id = i.warehouse_id " +
                "WHERE w.is_active = true " +
                "AND COALESCE(i.quantity - i.reserved_quantity, 0) < ? " +
                "AND COALESCE(i.quantity - i.reserved_quantity, 0) > 0 " +
                "ORDER BY available_quantity, w.name, p.name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, criticalLevel);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CriticalStock stock = new CriticalStock();
                    stock.setProductId(rs.getInt("product_id"));
                    stock.setSku(rs.getString("sku"));
                    stock.setProductName(rs.getString("product_name"));
                    stock.setUnit(rs.getString("unit"));
                    stock.setWarehouseId(rs.getInt("warehouse_id"));
                    stock.setWarehouseName(rs.getString("warehouse_name"));
                    stock.setAvailableQuantity(rs.getBigDecimal("available_quantity"));
                    stock.setCriticalLevel(criticalLevel);
                    criticalStocks.add(stock);
                }
            }
        }
        return criticalStocks;
    }

    public List<CriticalStock> findZeroStocks() throws SQLException {
        List<CriticalStock> zeroStocks = new ArrayList<>();
        String sql = "SELECT " +
                "p.id as product_id, p.sku, p.name as product_name, p.unit, " +
                "w.id as warehouse_id, w.name as warehouse_name, " +
                "COALESCE(i.quantity - i.reserved_quantity, 0) as available_quantity " +
                "FROM products p " +
                "CROSS JOIN warehouses w " +
                "LEFT JOIN inventory i ON p.id = i.product_id AND w.id = i.warehouse_id " +
                "WHERE w.is_active = true " +
                "AND COALESCE(i.quantity - i.reserved_quantity, 0) = 0 " +
                "ORDER BY w.name, p.name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                CriticalStock stock = new CriticalStock();
                stock.setProductId(rs.getInt("product_id"));
                stock.setSku(rs.getString("sku"));
                stock.setProductName(rs.getString("product_name"));
                stock.setUnit(rs.getString("unit"));
                stock.setWarehouseId(rs.getInt("warehouse_id"));
                stock.setWarehouseName(rs.getString("warehouse_name"));
                stock.setAvailableQuantity(rs.getBigDecimal("available_quantity"));
                stock.setCriticalLevel(BigDecimal.ZERO);
                zeroStocks.add(stock);
            }
        }
        return zeroStocks;
    }

    public void exportStockToFile(String filename) throws SQLException {
        List<TotalStockByProduct> stocks = getAllTotalStocks();

        if (stocks.isEmpty()) {
            System.out.println("\n❌ Нет данных для экспорта");
            return;
        }

        try {
            java.io.FileWriter fileWriter = new java.io.FileWriter(filename);
            java.io.PrintWriter printWriter = new java.io.PrintWriter(fileWriter);

            printWriter.println("ID;Артикул;Наименование;Категория;Общее количество;Резерв;Доступно");

            for (TotalStockByProduct stock : stocks) {
                printWriter.printf("%d;%s;%s;%s;%s;%s;%s%n",
                        stock.getProductId(),
                        stock.getSku(),
                        stock.getProductName(),
                        stock.getCategory() != null ? stock.getCategory() : "",
                        stock.getTotalQuantity() + " " + stock.getUnit(),
                        stock.getTotalReserved() + " " + stock.getUnit(),
                        stock.getTotalAvailable() + " " + stock.getUnit());
            }

            printWriter.close();

            java.io.File file = new java.io.File(filename);
            String absolutePath = file.getAbsolutePath();

            System.out.println("\n--- ЭКСПОРТ ОСТАТКОВ В ФАЙЛ ---");
            System.out.println("✅ Файл успешно создан!");
            System.out.println("📁 Путь к файлу: " + absolutePath);
            System.out.println("📊 Всего строк: " + stocks.size());

        } catch (java.io.IOException e) {
            System.err.println("\n❌ Ошибка при создании файла: " + e.getMessage());
            System.err.println("Проверьте права на запись в текущей директории");
        }
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
        System.out.println("║           КОНТРОЛЬ ОСТАТКОВ                ║");
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.println("║  1. Остатки по складу                      ║");
        System.out.println("║  2. Сводка по товару (все склады)          ║");
        System.out.println("║  3. Сводка по всем товарам                 ║");
        System.out.println("║  4. Критические остатки                    ║");
        System.out.println("║  5. Товары с нулевыми остатками            ║");
        System.out.println("║  6. Экспорт остатков в файл                ║");
        System.out.println("║  0. Вернуться к выбору модуля              ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.print("➜ Выберите действие: ");
    }

    public void start(Scanner scanner) {
        boolean running = true;

        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║       СИСТЕМА УПРАВЛЕНИЯ СКЛАДОМ           ║");
        System.out.println("║       Модуль: Контроль остатков            ║");
        System.out.println("╚════════════════════════════════════════════╝");

        while (running) {
            showMenu();
            int choice = safeNextInt(scanner);

            switch (choice) {
                case 1:
                    System.out.println("\n--- ОСТАТКИ ПО СКЛАДУ ---");

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

                    try {
                        List<StockByWarehouse> stocks = getStockByWarehouse(warehouseId);
                        if (stocks.isEmpty()) {
                            System.out.println("\n❌ На складе нет товаров или склад не существует");
                        } else {
                            System.out.println("\n┌───────┬────────────┬───────────────────────────┬──────────────┬──────────┬──────────┬──────────┐");
                            System.out.println("│ ID    │ Артикул    │ Наименование              │ Склад        │ Остаток  │ Резерв   │ Доступно │");
                            System.out.println("├───────┼────────────┼───────────────────────────┼──────────────┼──────────┼──────────┼──────────┤");
                            for (StockByWarehouse stock : stocks) {
                                stock.display();
                            }
                            System.out.println("└───────┴────────────┴───────────────────────────┴──────────────┴──────────┴──────────┴──────────┘");

                            BigDecimal totalQuantity = stocks.stream()
                                    .map(StockByWarehouse::getQuantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal totalReserved = stocks.stream()
                                    .map(StockByWarehouse::getReservedQuantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal totalAvailable = stocks.stream()
                                    .map(StockByWarehouse::getAvailableQuantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            System.out.println("\n📊 ИТОГО ПО СКЛАДУ:");
                            System.out.println("   Общий остаток: " + totalQuantity + " ед.");
                            System.out.println("   Зарезервировано: " + totalReserved + " ед.");
                            System.out.println("   Доступно к отгрузке: " + totalAvailable + " ед.");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 2:
                    System.out.println("\n--- СВОДКА ПО ТОВАРУ ---");

                    System.out.println("\nДОСТУПНЫЕ ТОВАРЫ:");
                    System.out.println("┌───────┬────────────┬──────────────────────────────────────────┬───────┐");
                    System.out.println("│ ID    │ Артикул    │ Наименование                             │ Ед.   │");
                    System.out.println("├───────┼────────────┼──────────────────────────────────────────┼───────┤");
                    try {
                        List<ProductInfo> products = getAllProducts();
                        for (ProductInfo p : products) {
                            p.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    System.out.println("└───────┴────────────┴──────────────────────────────────────────┴───────┘");

                    System.out.print("\nВведите ID товара: ");
                    int productId = safeNextInt(scanner);

                    try {
                        TotalStockByProduct stock = getTotalStockByProduct(productId);
                        if (stock == null) {
                            System.out.println("\n❌ Товар не найден");
                        } else {
                            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
                            System.out.println("║                  СВОДКА ПО ТОВАРУ                          ║");
                            System.out.println("╠════════════════════════════════════════════════════════════╣");
                            System.out.println("║ ID товара: " + stock.getProductId());
                            System.out.println("║ Артикул: " + stock.getSku());
                            System.out.println("║ Наименование: " + stock.getProductName());
                            System.out.println("║ Категория: " + (stock.getCategory() != null ? stock.getCategory() : "-"));
                            System.out.println("╠════════════════════════════════════════════════════════════╣");
                            System.out.println("║ Общее количество (все склады): " + stock.getTotalQuantity() + " " + stock.getUnit());
                            System.out.println("║ Зарезервировано (все склады): " + stock.getTotalReserved() + " " + stock.getUnit());
                            System.out.println("║ Доступно к отгрузке (все склады): " + stock.getTotalAvailable() + " " + stock.getUnit());
                            System.out.println("╚════════════════════════════════════════════════════════════╝");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 3:
                    System.out.println("\n--- СВОДКА ПО ВСЕМ ТОВАРАМ ---");

                    try {
                        List<TotalStockByProduct> allStocks = getAllTotalStocks();
                        if (allStocks.isEmpty()) {
                            System.out.println("\n❌ Товары не найдены");
                        } else {
                            System.out.println("\n┌───────┬────────────┬──────────────────────────────────┬──────────────┬────────────┬────────────┐");
                            System.out.println("│ ID    │ Артикул    │ Наименование                     │ Общее кол-во │ Резерв     │ Доступно   │");
                            System.out.println("├───────┼────────────┼──────────────────────────────────┼──────────────┼────────────┼────────────┤");
                            for (TotalStockByProduct s : allStocks) {
                                s.display();
                            }
                            System.out.println("└───────┴────────────┴──────────────────────────────────┴──────────────┴────────────┴────────────┘");

                            BigDecimal totalAllQuantity = allStocks.stream()
                                    .map(TotalStockByProduct::getTotalQuantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal totalAllReserved = allStocks.stream()
                                    .map(TotalStockByProduct::getTotalReserved)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal totalAllAvailable = allStocks.stream()
                                    .map(TotalStockByProduct::getTotalAvailable)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            System.out.println("\n📊 ОБЩАЯ СТАТИСТИКА ПО ВСЕМ СКЛАДАМ:");
                            System.out.println("   Всего товаров на складах: " + totalAllQuantity + " ед.");
                            System.out.println("   Зарезервировано всего: " + totalAllReserved + " ед.");
                            System.out.println("   Доступно к отгрузке всего: " + totalAllAvailable + " ед.");
                            System.out.println("   Количество позиций: " + allStocks.size());
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 4:
                    System.out.println("\n--- КРИТИЧЕСКИЕ ОСТАТКИ ---");
                    System.out.print("Введите критический уровень (минимальное количество): ");
                    BigDecimal criticalLevel = safeNextBigDecimal(scanner);

                    try {
                        List<CriticalStock> criticalStocks = findCriticalStocks(criticalLevel);
                        if (criticalStocks.isEmpty()) {
                            System.out.println("\n✅ Товары с остатками ниже " + criticalLevel + " не найдены");
                        } else {
                            System.out.println("\n⚠️  ВНИМАНИЕ! Товары с критическими остатками:");
                            System.out.println("┌───────┬────────────┬───────────────────────────┬──────────────┬────────────┬──────────────┐");
                            System.out.println("│ ID    │ Артикул    │ Наименование              │ Склад        │ Остаток    │ Критический │");
                            System.out.println("├───────┼────────────┼───────────────────────────┼──────────────┼────────────┼──────────────┤");
                            for (CriticalStock cs : criticalStocks) {
                                cs.display();
                            }
                            System.out.println("└───────┴────────────┴───────────────────────────┴──────────────┴────────────┴──────────────┘");
                            System.out.println("\n⚠️  Рекомендуется пополнить запасы по указанным позициям!");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("\n--- ТОВАРЫ С НУЛЕВЫМИ ОСТАТКАМИ ---");

                    try {
                        List<CriticalStock> zeroStocks = findZeroStocks();
                        if (zeroStocks.isEmpty()) {
                            System.out.println("\n✅ Товары с нулевыми остатками не найдены");
                        } else {
                            System.out.println("\n⚠️  Товары, которых нет в наличии:");
                            System.out.println("┌───────┬────────────┬───────────────────────────┬──────────────┬────────────┐");
                            System.out.println("│ ID    │ Артикул    │ Наименование              │ Склад        │ Остаток    │");
                            System.out.println("├───────┼────────────┼───────────────────────────┼──────────────┼────────────┤");
                            for (CriticalStock zs : zeroStocks) {
                                System.out.printf("│ %-5d │ %-10s │ %-25s │ %-12s │ %-10s │%n",
                                        zs.getProductId(),
                                        zs.getSku(),
                                        zs.getProductName().length() > 25 ? zs.getProductName().substring(0, 22) + "..." : zs.getProductName(),
                                        zs.getWarehouseName().length() > 12 ? zs.getWarehouseName().substring(0, 9) + "..." : zs.getWarehouseName(),
                                        "0 " + zs.getUnit());
                            }
                            System.out.println("└───────┴────────────┴───────────────────────────┴──────────────┴────────────┘");
                            System.out.println("\n⚠️  Требуется срочное пополнение складов!");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 6:
                    System.out.println("\n--- ЭКСПОРТ ОСТАТКОВ ---");
                    System.out.print("Введите имя файла (например, report.txt): ");
                    String filename = scanner.nextLine().trim();
                    if (filename.isEmpty()) {
                        filename = "stock_report_" + System.currentTimeMillis() + ".txt";
                    }
                    try {
                        exportStockToFile(filename);
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
                    System.out.println("\n❌ Неверный выбор! Пожалуйста, выберите действие от 0 до 6");
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