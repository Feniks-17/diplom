import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProductManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Sklad";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Vvedensky2004";

    private Connection connection;

    public ProductManager() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ============================================================
    // 1. Создание товара
    // ============================================================
    public Product createProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (sku, name, description, category, unit) VALUES (?, ?, ?, ?, ?) RETURNING id, created_at, updated_at";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getSku());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getDescription());
            pstmt.setString(4, product.getCategory());
            pstmt.setString(5, product.getUnit());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    product.setId(rs.getInt("id"));
                    product.setCreatedAt(rs.getTimestamp("created_at"));
                    product.setUpdatedAt(rs.getTimestamp("updated_at"));
                    System.out.println("\n✓ Товар успешно создан с ID: " + product.getId());
                    return product;
                }
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.err.println("\n❌ Ошибка: Товар с артикулом '" + product.getSku() + "' уже существует");
            } else {
                throw e;
            }
        }
        return null;
    }

    // ============================================================
    // 2. Поиск товара по артикулу
    // ============================================================
    public Product findProductBySku(String sku) throws SQLException {
        String sql = "SELECT * FROM products WHERE sku = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sku);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractProductFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // ============================================================
    // 3. Поиск товаров по названию
    // ============================================================
    public List<Product> findProductsByName(String namePattern) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name ILIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + namePattern + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(extractProductFromResultSet(rs));
                }
            }
        }
        return products;
    }

    // ============================================================
    // 4. Поиск товаров по категории
    // ============================================================
    public List<Product> findProductsByCategory(String category) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(extractProductFromResultSet(rs));
                }
            }
        }
        return products;
    }

    // ============================================================
    // 5. Получение всех товаров
    // ============================================================
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        }
        return products;
    }

    // ============================================================
    // 6. Редактирование товара
    // ============================================================
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, description = ?, category = ?, unit = ?, updated_at = CURRENT_TIMESTAMP WHERE sku = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setString(3, product.getCategory());
            pstmt.setString(4, product.getUnit());
            pstmt.setString(5, product.getSku());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("\n✓ Товар с артикулом '" + product.getSku() + "' успешно обновлен");
                return true;
            } else {
                System.out.println("\n❌ Товар с артикулом '" + product.getSku() + "' не найден");
                return false;
            }
        }
    }

    // ============================================================
    // 7. Удаление товара
    // ============================================================
    public boolean deleteProduct(String sku) throws SQLException {
        String checkInventorySql = "SELECT COUNT(*) FROM inventory i JOIN products p ON p.id = i.product_id WHERE p.sku = ? AND (i.quantity > 0 OR i.reserved_quantity > 0)";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkInventorySql)) {
            checkStmt.setString(1, sku);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("\n❌ Невозможно удалить товар: существуют ненулевые остатки на складах");
                    return false;
                }
            }
        }

        String sql = "DELETE FROM products WHERE sku = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sku);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("\n✓ Товар с артикулом '" + sku + "' успешно удален");
                return true;
            } else {
                System.out.println("\n❌ Товар с артикулом '" + sku + "' не найден");
                return false;
            }
        }
    }

    // ============================================================
    // Вспомогательный метод для извлечения товара из ResultSet
    // ============================================================
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setSku(rs.getString("sku"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setCategory(rs.getString("category"));
        product.setUnit(rs.getString("unit"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setUpdatedAt(rs.getTimestamp("updated_at"));
        return product;
    }

    // ============================================================
    // Реализация AutoCloseable
    // ============================================================
    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Соединение с БД закрыто");
        }
    }

    // ============================================================
    // Класс Product (модель данных)
    // ============================================================
    public static class Product {
        private int id;
        private String sku;
        private String name;
        private String description;
        private String category;
        private String unit;
        private Timestamp createdAt;
        private Timestamp updatedAt;

        public Product() {}

        public Product(String sku, String name, String description, String category, String unit) {
            this.sku = sku;
            this.name = name;
            this.description = description;
            this.category = category;
            this.unit = unit;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

        public Timestamp getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

        public void display() {
            System.out.println("┌─────────────────────────────────────");
            System.out.println("│ ID: " + id);
            System.out.println("│ Артикул: " + sku);
            System.out.println("│ Название: " + name);
            System.out.println("│ Категория: " + (category != null ? category : "-"));
            System.out.println("│ Описание: " + (description != null ? description : "-"));
            System.out.println("│ Ед. измерения: " + unit);
            System.out.println("│ Создан: " + createdAt);
            System.out.println("│ Обновлен: " + updatedAt);
            System.out.println("└─────────────────────────────────────");
        }
    }

    // ============================================================
    // Метод для отображения меню
    // ============================================================
    private void showMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       УПРАВЛЕНИЕ ТОВАРАМИ              ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║  1. Создать товар                      ║");
        System.out.println("║  2. Найти товар по артикулу            ║");
        System.out.println("║  3. Найти товары по названию           ║");
        System.out.println("║  4. Найти товары по категории          ║");
        System.out.println("║  5. Показать все товары                ║");
        System.out.println("║  6. Редактировать товар                ║");
        System.out.println("║  7. Удалить товар                      ║");
        System.out.println("║  0. Вернуться к выбору модуля          ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.print("➜ Выберите действие: ");
    }

    // ============================================================
    // Метод start() - запуск модуля
    // ============================================================
    public void start(Scanner scanner) {
        boolean running = true;

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     СИСТЕМА УПРАВЛЕНИЯ СКЛАДОМ         ║");
        System.out.println("║     Модуль: Управление товарами        ║");
        System.out.println("╚════════════════════════════════════════╝");

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
                    System.out.println("\n--- СОЗДАНИЕ НОВОГО ТОВАРА ---");
                    System.out.print("Артикул (SKU): ");
                    String sku = scanner.nextLine().trim();
                    if (sku.isEmpty()) {
                        System.out.println("❌ Артикул не может быть пустым!");
                        break;
                    }

                    System.out.print("Название: ");
                    String name = scanner.nextLine().trim();
                    if (name.isEmpty()) {
                        System.out.println("❌ Название не может быть пустым!");
                        break;
                    }

                    System.out.print("Описание: ");
                    String description = scanner.nextLine().trim();

                    System.out.print("Категория: ");
                    String category = scanner.nextLine().trim();

                    System.out.print("Единица измерения (по умолчанию 'шт'): ");
                    String unit = scanner.nextLine().trim();
                    if (unit.isEmpty()) unit = "шт";

                    Product newProduct = new Product(sku, name, description, category, unit);
                    try {
                        Product created = createProduct(newProduct);
                        if (created != null) {
                            created.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 2:
                    System.out.println("\n--- ПОИСК ТОВАРА ПО АРТИКУЛУ ---");
                    System.out.print("Введите артикул: ");
                    String searchSku = scanner.nextLine().trim();
                    if (searchSku.isEmpty()) {
                        System.out.println("❌ Артикул не может быть пустым!");
                        break;
                    }
                    try {
                        Product found = findProductBySku(searchSku);
                        if (found != null) {
                            System.out.println("\n✅ Товар найден:");
                            found.display();
                        } else {
                            System.out.println("\n❌ Товар с артикулом '" + searchSku + "' не найден");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 3:
                    System.out.println("\n--- ПОИСК ТОВАРОВ ПО НАЗВАНИЮ ---");
                    System.out.print("Введите фрагмент названия: ");
                    String namePattern = scanner.nextLine().trim();
                    if (namePattern.isEmpty()) {
                        System.out.println("❌ Введите название для поиска!");
                        break;
                    }
                    try {
                        List<Product> byName = findProductsByName(namePattern);
                        if (byName.isEmpty()) {
                            System.out.println("\n❌ Товары не найдены");
                        } else {
                            System.out.println("\n✅ Найдено товаров: " + byName.size());
                            for (int i = 0; i < byName.size(); i++) {
                                System.out.println("\n--- Товар " + (i + 1) + " ---");
                                byName.get(i).display();
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 4:
                    System.out.println("\n--- ПОИСК ТОВАРОВ ПО КАТЕГОРИИ ---");
                    System.out.print("Введите категорию: ");
                    String cat = scanner.nextLine().trim();
                    if (cat.isEmpty()) {
                        System.out.println("❌ Введите категорию для поиска!");
                        break;
                    }
                    try {
                        List<Product> byCategory = findProductsByCategory(cat);
                        if (byCategory.isEmpty()) {
                            System.out.println("\n❌ Товары в категории '" + cat + "' не найдены");
                        } else {
                            System.out.println("\n✅ Найдено товаров: " + byCategory.size());
                            for (int i = 0; i < byCategory.size(); i++) {
                                System.out.println("\n--- Товар " + (i + 1) + " ---");
                                byCategory.get(i).display();
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("\n--- ВСЕ ТОВАРЫ ---");
                    try {
                        List<Product> all = getAllProducts();
                        if (all.isEmpty()) {
                            System.out.println("\n❌ Товары не найдены");
                        } else {
                            System.out.println("\n✅ Всего товаров в базе: " + all.size());
                            for (int i = 0; i < all.size(); i++) {
                                System.out.println("\n--- Товар " + (i + 1) + " ---");
                                all.get(i).display();
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 6:
                    System.out.println("\n--- РЕДАКТИРОВАНИЕ ТОВАРА ---");
                    System.out.print("Введите артикул товара для редактирования: ");
                    String editSku = scanner.nextLine().trim();
                    if (editSku.isEmpty()) {
                        System.out.println("❌ Артикул не может быть пустым!");
                        break;
                    }
                    try {
                        Product toEdit = findProductBySku(editSku);
                        if (toEdit != null) {
                            System.out.println("\nТекущие данные товара:");
                            toEdit.display();
                            System.out.println("\nВведите новые данные (оставьте пустым, чтобы не менять):");

                            System.out.print("Новое название [" + toEdit.getName() + "]: ");
                            String newName = scanner.nextLine().trim();
                            if (!newName.isEmpty()) toEdit.setName(newName);

                            System.out.print("Новое описание [" + (toEdit.getDescription() != null ? toEdit.getDescription() : "-") + "]: ");
                            String newDesc = scanner.nextLine().trim();
                            if (!newDesc.isEmpty()) toEdit.setDescription(newDesc);

                            System.out.print("Новая категория [" + (toEdit.getCategory() != null ? toEdit.getCategory() : "-") + "]: ");
                            String newCat = scanner.nextLine().trim();
                            if (!newCat.isEmpty()) toEdit.setCategory(newCat);

                            System.out.print("Новая единица измерения [" + toEdit.getUnit() + "]: ");
                            String newUnit = scanner.nextLine().trim();
                            if (!newUnit.isEmpty()) toEdit.setUnit(newUnit);

                            updateProduct(toEdit);
                        } else {
                            System.out.println("\n❌ Товар с артикулом '" + editSku + "' не найден");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 7:
                    System.out.println("\n--- УДАЛЕНИЕ ТОВАРА ---");
                    System.out.print("Введите артикул товара для удаления: ");
                    String delSku = scanner.nextLine().trim();
                    if (delSku.isEmpty()) {
                        System.out.println("❌ Артикул не может быть пустым!");
                        break;
                    }

                    try {
                        Product toDelete = findProductBySku(delSku);
                        if (toDelete == null) {
                            System.out.println("\n❌ Товар с артикулом '" + delSku + "' не найден");
                            break;
                        }

                        System.out.println("\nТовар, который будет удален:");
                        toDelete.display();
                        System.out.print("\nВы уверены, что хотите удалить этот товар? (yes/no): ");
                        String confirm = scanner.nextLine().trim();
                        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
                            deleteProduct(delSku);
                        } else {
                            System.out.println("\n❌ Операция удаления отменена");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 0:
                    System.out.println("\n╔════════════════════════════════════════╗");
                    System.out.println("║     Возврат в главное меню...          ║");
                    System.out.println("╚════════════════════════════════════════╝");
                    running = false;
                    break;

                default:
                    System.out.println("\n❌ Неверный выбор! Пожалуйста, выберите действие от 0 до 7");
                    break;
            }

            if (running && choice != 0) {
                System.out.println("\n────────────────────────────────────────");
                System.out.print("Нажмите Enter, чтобы продолжить...");
                scanner.nextLine();
            }
        }
    }
}