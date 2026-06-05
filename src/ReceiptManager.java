import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.util.Date;

public class ReceiptManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Sklad";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Vvedensky2004";

    private Connection connection;

    public ReceiptManager() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static class Receipt {
        private int id;
        private String receiptNumber;
        private Integer supplierId;
        private String supplierName;
        private int warehouseId;
        private String warehouseName;
        private Date receiptDate;
        private String status;
        private Timestamp createdAt;
        private List<ReceiptItem> items;

        public Receipt() {
            this.items = new ArrayList<>();
            this.status = "draft";
            this.receiptDate = new Date(System.currentTimeMillis());
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getReceiptNumber() { return receiptNumber; }
        public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
        public Integer getSupplierId() { return supplierId; }
        public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public int getWarehouseId() { return warehouseId; }
        public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public Date getReceiptDate() { return receiptDate; }
        public void setReceiptDate(Date receiptDate) { this.receiptDate = receiptDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
        public List<ReceiptItem> getItems() { return items; }
        public void setItems(List<ReceiptItem> items) { this.items = items; }
        public void addItem(ReceiptItem item) { this.items.add(item); }

        public BigDecimal getTotalAmount() {
            return items.stream()
                    .map(ReceiptItem::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public void display() {
            System.out.println("┌─────────────────────────────────────────────────");
            System.out.println("│ ПРИХОДНАЯ НАКЛАДНАЯ");
            System.out.println("│ Номер: " + receiptNumber);
            System.out.println("│ ID: " + id);
            System.out.println("│ Дата: " + receiptDate);
            System.out.println("│ Поставщик: " + (supplierName != null ? supplierName : "-"));
            System.out.println("│ Склад: " + warehouseName);
            System.out.println("│ Статус: " + getStatusText());
            System.out.println("│ Создана: " + createdAt);
            System.out.println("│");
            System.out.println("│ ТОВАРЫ:");
            System.out.println("│ ┌────┬──────────────────────┬──────────┬────────────┬──────────────┐");
            System.out.println("│ │ №  │ Наименование         │ Кол-во   │ Цена       │ Сумма        │");
            System.out.println("│ ├────┼──────────────────────┼──────────┼────────────┼──────────────┤");
            for (int i = 0; i < items.size(); i++) {
                ReceiptItem item = items.get(i);
                System.out.printf("│ │ %-2d │ %-20s │ %-8s │ %-10s │ %-12s │%n",
                        i + 1,
                        item.getProductName().length() > 20 ? item.getProductName().substring(0, 17) + "..." : item.getProductName(),
                        item.getQuantity() + " " + item.getUnit(),
                        item.getUnitPrice() + " руб.",
                        item.getTotalAmount() + " руб.");
            }
            System.out.println("│ └────┴──────────────────────┴──────────┴────────────┴──────────────┘");
            System.out.println("│");
            System.out.println("│ ИТОГО: " + getTotalAmount() + " руб.");
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

    public static class ReceiptItem {
        private int id;
        private int receiptId;
        private int productId;
        private String productName;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;

        public ReceiptItem() {}

        public ReceiptItem(int productId, BigDecimal quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getReceiptId() { return receiptId; }
        public void setReceiptId(int receiptId) { this.receiptId = receiptId; }
        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
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
            System.out.printf("│ %-5d │ %-10s │ %-30s │ %-5s │%n", id, sku,
                    name.length() > 30 ? name.substring(0, 27) + "..." : name, unit);
        }
    }

    public Receipt createReceipt(String receiptNumber, Integer supplierId, int warehouseId, Date receiptDate) throws SQLException {
        String sql = "INSERT INTO receipts (receipt_number, supplier_id, warehouse_id, receipt_date, status) VALUES (?, ?, ?, ?, 'draft') RETURNING id, created_at";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, receiptNumber);
            if (supplierId != null) {
                pstmt.setInt(2, supplierId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setInt(3, warehouseId);
            pstmt.setDate(4, new java.sql.Date(receiptDate.getTime()));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Receipt receipt = new Receipt();
                    receipt.setId(rs.getInt("id"));
                    receipt.setReceiptNumber(receiptNumber);
                    receipt.setSupplierId(supplierId);
                    receipt.setWarehouseId(warehouseId);
                    receipt.setReceiptDate(receiptDate);
                    receipt.setStatus("draft");
                    receipt.setCreatedAt(rs.getTimestamp("created_at"));

                    loadReceiptNames(receipt);

                    System.out.println("\n✓ Приходная накладная создана с ID: " + receipt.getId());
                    return receipt;
                }
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.err.println("\n❌ Ошибка: Накладная с номером '" + receiptNumber + "' уже существует");
            } else {
                throw e;
            }
        }
        return null;
    }

    public boolean addReceiptItem(int receiptId, int productId, BigDecimal quantity, BigDecimal unitPrice) throws SQLException {
        if (!isReceiptDraft(receiptId)) {
            System.err.println("\n❌ Невозможно добавить товар: накладная не в статусе 'Черновик'");
            return false;
        }

        String sql = "INSERT INTO receipt_items (receipt_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, receiptId);
            pstmt.setInt(2, productId);
            pstmt.setBigDecimal(3, quantity);
            pstmt.setBigDecimal(4, unitPrice);

            pstmt.executeUpdate();
            System.out.println("\n✓ Товар добавлен в накладную");
            return true;
        } catch (SQLException e) {
            System.err.println("\n❌ Ошибка при добавлении товара: " + e.getMessage());
            return false;
        }
    }

    public Receipt getReceiptById(int id) throws SQLException {
        String sql = "SELECT r.*, w.name as warehouse_name, s.name as supplier_name " +
                "FROM receipts r " +
                "LEFT JOIN warehouses w ON r.warehouse_id = w.id " +
                "LEFT JOIN suppliers s ON r.supplier_id = s.id " +
                "WHERE r.id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Receipt receipt = new Receipt();
                    receipt.setId(rs.getInt("id"));
                    receipt.setReceiptNumber(rs.getString("receipt_number"));
                    receipt.setSupplierId(rs.getInt("supplier_id"));
                    if (rs.wasNull()) receipt.setSupplierId(null);
                    receipt.setSupplierName(rs.getString("supplier_name"));
                    receipt.setWarehouseId(rs.getInt("warehouse_id"));
                    receipt.setWarehouseName(rs.getString("warehouse_name"));
                    receipt.setReceiptDate(rs.getDate("receipt_date"));
                    receipt.setStatus(rs.getString("status"));
                    receipt.setCreatedAt(rs.getTimestamp("created_at"));

                    loadReceiptItems(receipt);

                    return receipt;
                }
            }
        }
        return null;
    }

    public List<Receipt> getAllReceipts() throws SQLException {
        List<Receipt> receipts = new ArrayList<>();
        String sql = "SELECT r.*, w.name as warehouse_name, s.name as supplier_name " +
                "FROM receipts r " +
                "LEFT JOIN warehouses w ON r.warehouse_id = w.id " +
                "LEFT JOIN suppliers s ON r.supplier_id = s.id " +
                "ORDER BY r.id DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Receipt receipt = new Receipt();
                receipt.setId(rs.getInt("id"));
                receipt.setReceiptNumber(rs.getString("receipt_number"));
                receipt.setSupplierId(rs.getInt("supplier_id"));
                if (rs.wasNull()) receipt.setSupplierId(null);
                receipt.setSupplierName(rs.getString("supplier_name"));
                receipt.setWarehouseId(rs.getInt("warehouse_id"));
                receipt.setWarehouseName(rs.getString("warehouse_name"));
                receipt.setReceiptDate(rs.getDate("receipt_date"));
                receipt.setStatus(rs.getString("status"));
                receipt.setCreatedAt(rs.getTimestamp("created_at"));

                receipts.add(receipt);
            }
        }
        return receipts;
    }

    public boolean postReceipt(int receiptId) throws SQLException {
        if (!isReceiptDraft(receiptId)) {
            System.err.println("\n❌ Невозможно провести: накладная не в статусе 'Черновик'");
            return false;
        }

        Receipt receipt = getReceiptById(receiptId);
        if (receipt == null) {
            System.err.println("\n❌ Накладная не найдена");
            return false;
        }

        if (receipt.getItems().isEmpty()) {
            System.err.println("\n❌ Невозможно провести: накладная не содержит товаров");
            return false;
        }

        if (!isWarehouseActive(receipt.getWarehouseId())) {
            System.err.println("\n❌ Невозможно провести: склад неактивен");
            return false;
        }

        if (receipt.getSupplierId() != null && !isSupplierActive(receipt.getSupplierId())) {
            System.err.println("\n❌ Невозможно провести: поставщик неактивен");
            return false;
        }

        connection.setAutoCommit(false);

        try {
            for (ReceiptItem item : receipt.getItems()) {
                updateInventory(receipt.getWarehouseId(), item.getProductId(), item.getQuantity());
            }

            String sql = "UPDATE receipts SET status = 'posted' WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, receiptId);
                pstmt.executeUpdate();
            }

            connection.commit();
            System.out.println("\n✅ Накладная успешно проведена! Остатки на складе увеличены.");
            return true;

        } catch (SQLException e) {
            connection.rollback();
            System.err.println("\n❌ Ошибка при проведении накладной: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public boolean cancelReceipt(int receiptId) throws SQLException {
        if (!isReceiptDraft(receiptId)) {
            System.err.println("\n❌ Невозможно отменить: накладная не в статусе 'Черновик'");
            return false;
        }

        String sql = "UPDATE receipts SET status = 'cancelled' WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, receiptId);
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

    public boolean removeReceiptItem(int receiptId, int itemId) throws SQLException {
        if (!isReceiptDraft(receiptId)) {
            System.err.println("\n❌ Невозможно удалить позицию: накладная не в статусе 'Черновик'");
            return false;
        }

        String sql = "DELETE FROM receipt_items WHERE id = ? AND receipt_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, receiptId);
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

    private boolean isReceiptDraft(int receiptId) throws SQLException {
        String sql = "SELECT status FROM receipts WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, receiptId);
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

    private boolean isSupplierActive(int supplierId) throws SQLException {
        String sql = "SELECT is_active FROM suppliers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_active");
                }
            }
        }
        return false;
    }

    private void updateInventory(int warehouseId, int productId, BigDecimal quantity) throws SQLException {
        String sql = "INSERT INTO inventory (warehouse_id, product_id, quantity, reserved_quantity) " +
                "VALUES (?, ?, ?, 0) " +
                "ON CONFLICT (product_id, warehouse_id) " +
                "DO UPDATE SET quantity = inventory.quantity + EXCLUDED.quantity, updated_at = CURRENT_TIMESTAMP";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);
            pstmt.setInt(2, productId);
            pstmt.setBigDecimal(3, quantity);
            pstmt.executeUpdate();
        }
    }

    private void loadReceiptNames(Receipt receipt) throws SQLException {
        String warehouseSql = "SELECT name FROM warehouses WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(warehouseSql)) {
            pstmt.setInt(1, receipt.getWarehouseId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    receipt.setWarehouseName(rs.getString("name"));
                }
            }
        }

        if (receipt.getSupplierId() != null) {
            String supplierSql = "SELECT name FROM suppliers WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(supplierSql)) {
                pstmt.setInt(1, receipt.getSupplierId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        receipt.setSupplierName(rs.getString("name"));
                    }
                }
            }
        }
    }

    private void loadReceiptItems(Receipt receipt) throws SQLException {
        String sql = "SELECT ri.*, p.name as product_name, p.unit " +
                "FROM receipt_items ri " +
                "JOIN products p ON ri.product_id = p.id " +
                "WHERE ri.receipt_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, receipt.getId());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ReceiptItem item = new ReceiptItem();
                    item.setId(rs.getInt("id"));
                    item.setReceiptId(rs.getInt("receipt_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnit(rs.getString("unit"));
                    item.setQuantity(rs.getBigDecimal("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalAmount(rs.getBigDecimal("total_amount"));

                    receipt.addItem(item);
                }
            }
        }
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

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Соединение с БД закрыто");
        }
    }

    private void showMenu() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║           ПРИХОД ТОВАРА                    ║");
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.println("║  1. Создать приходную накладную            ║");
        System.out.println("║  2. Добавить товар в накладную             ║");
        System.out.println("║  3. Просмотреть накладную                  ║");
        System.out.println("║  4. Показать все накладные                 ║");
        System.out.println("║  5. Провести накладную                     ║");
        System.out.println("║  6. Отменить накладную                     ║");
        System.out.println("║  7. Удалить позицию из накладной           ║");
        System.out.println("║  0. Вернуться к выбору модуля              ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.print("➜ Выберите действие: ");
    }

    public void start(Scanner scanner) {
        boolean running = true;
        Integer currentReceiptId = null;

        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║       СИСТЕМА УПРАВЛЕНИЯ СКЛАДОМ           ║");
        System.out.println("║       Модуль: Приход товара                ║");
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
                    System.out.println("\n--- СОЗДАНИЕ ПРИХОДНОЙ НАКЛАДНОЙ ---");
                    System.out.print("Номер накладной: ");
                    String number = scanner.nextLine().trim();
                    if (number.isEmpty()) {
                        System.out.println("❌ Номер не может быть пустым!");
                        break;
                    }

                    System.out.print("ID поставщика (0 - без поставщика): ");
                    Integer supplierId = null;
                    try {
                        int supId = scanner.nextInt();
                        if (supId > 0) supplierId = supId;
                        scanner.nextLine();
                    } catch (Exception e) {
                        scanner.nextLine();
                    }

                    System.out.print("ID склада: ");
                    int warehouseId;
                    try {
                        warehouseId = scanner.nextInt();
                        scanner.nextLine();
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка: Введите корректный ID склада!");
                        scanner.nextLine();
                        break;
                    }

                    try {
                        Receipt receipt = createReceipt(number, supplierId, warehouseId, new Date(System.currentTimeMillis()));
                        if (receipt != null) {
                            currentReceiptId = receipt.getId();
                            receipt.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 2:
                    if (currentReceiptId == null) {
                        System.out.println("\n⚠️  Сначала создайте накладную (пункт 1)");
                        break;
                    }

                    System.out.println("\n--- ДОБАВЛЕНИЕ ТОВАРА В НАКЛАДНУЮ ---");
                    System.out.println("Текущая накладная ID: " + currentReceiptId);

                    System.out.println("\nДОСТУПНЫЕ ТОВАРЫ:");
                    System.out.println("┌───────┬────────────┬────────────────────────────────┬───────┐");
                    System.out.println("│ ID    │ Артикул    │ Название                       │ Ед.   │");
                    System.out.println("├───────┼────────────┼────────────────────────────────┼───────┤");
                    try {
                        List<ProductInfo> products = getAllProducts();
                        for (ProductInfo p : products) {
                            p.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    System.out.println("└───────┴────────────┴────────────────────────────────┴───────┘");

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

                    System.out.print("Количество: ");
                    BigDecimal quantity;
                    try {
                        quantity = scanner.nextBigDecimal();
                        scanner.nextLine();
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка ввода!");
                        scanner.nextLine();
                        break;
                    }

                    System.out.print("Цена за единицу: ");
                    BigDecimal price;
                    try {
                        price = scanner.nextBigDecimal();
                        scanner.nextLine();
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка ввода!");
                        scanner.nextLine();
                        break;
                    }

                    try {
                        addReceiptItem(currentReceiptId, productId, quantity, price);
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 3:
                    System.out.println("\n--- ПРОСМОТР НАКЛАДНОЙ ---");
                    System.out.print("Введите ID накладной (Enter - текущая " + currentReceiptId + "): ");
                    String input = scanner.nextLine().trim();
                    int viewId;
                    if (input.isEmpty() && currentReceiptId != null) {
                        viewId = currentReceiptId;
                    } else {
                        try {
                            viewId = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Ошибка ввода!");
                            break;
                        }
                    }

                    try {
                        Receipt viewReceipt = getReceiptById(viewId);
                        if (viewReceipt != null) {
                            viewReceipt.display();
                        } else {
                            System.out.println("\n❌ Накладная не найдена");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 4:
                    System.out.println("\n--- ВСЕ ПРИХОДНЫЕ НАКЛАДНЫЕ ---");
                    try {
                        List<Receipt> allReceipts = getAllReceipts();
                        if (allReceipts.isEmpty()) {
                            System.out.println("\n❌ Накладные не найдены");
                        } else {
                            System.out.println("\n✅ Всего накладных: " + allReceipts.size());
                            for (Receipt r : allReceipts) {
                                System.out.println("\n┌─────────────────────────────────────────────────");
                                System.out.println("│ Накладная #" + r.getId());
                                System.out.println("│ Номер: " + r.getReceiptNumber());
                                System.out.println("│ Дата: " + r.getReceiptDate());
                                System.out.println("│ Склад: " + r.getWarehouseName());
                                System.out.println("│ Статус: " + r.getStatus());
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
                        postReceipt(postId);
                        if (currentReceiptId != null && currentReceiptId == postId) {
                            currentReceiptId = null;
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
                        cancelReceipt(cancelId);
                        if (currentReceiptId != null && currentReceiptId == cancelId) {
                            currentReceiptId = null;
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Ошибка ввода!");
                        scanner.nextLine();
                    }
                    break;

                case 7:
                    if (currentReceiptId == null) {
                        System.out.println("\n⚠️  Нет текущей накладной");
                        break;
                    }

                    System.out.println("\n--- УДАЛЕНИЕ ПОЗИЦИИ ИЗ НАКЛАДНОЙ ---");
                    System.out.println("Текущая накладная ID: " + currentReceiptId);

                    try {
                        Receipt current = getReceiptById(currentReceiptId);
                        if (current != null && !current.getItems().isEmpty()) {
                            System.out.println("\nПозиции в накладной:");
                            for (int i = 0; i < current.getItems().size(); i++) {
                                ReceiptItem item = current.getItems().get(i);
                                System.out.println((i + 1) + ". ID=" + item.getId() + " - " +
                                        item.getProductName() + " - " + item.getQuantity() + " " + item.getUnit());
                            }
                            System.out.print("\nВведите ID позиции для удаления: ");
                            try {
                                int itemId = scanner.nextInt();
                                scanner.nextLine();
                                removeReceiptItem(currentReceiptId, itemId);
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

                case 0:
                    System.out.println("\n╔════════════════════════════════════════════╗");
                    System.out.println("║         Возврат в главное меню...          ║");
                    System.out.println("╚════════════════════════════════════════════╝");
                    running = false;
                    break;

                default:
                    System.out.println("\n❌ Неверный выбор! Пожалуйста, выберите действие от 0 до 7");
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