import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WarehouseManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Sklad";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Vvedensky2004";

    private Connection connection;

    public WarehouseManager() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static class Warehouse {
        private int id;
        private String name;
        private String location;
        private boolean isActive;
        private Timestamp createdAt;
        private Timestamp updatedAt;

        public Warehouse() {}

        public Warehouse(String name, String location) {
            this.name = name;
            this.location = location;
            this.isActive = true;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
        public Timestamp getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

        public void display() {
            System.out.println("┌─────────────────────────────────────");
            System.out.println("│ ID склада: " + id);
            System.out.println("│ Название: " + name);
            System.out.println("│ Адрес: " + (location != null ? location : "-"));
            System.out.println("│ Статус: " + (isActive ? "✅ Активен" : "❌ Неактивен"));
            if (createdAt != null) {
                System.out.println("│ Создан: " + createdAt);
            }
            if (updatedAt != null) {
                System.out.println("│ Обновлен: " + updatedAt);
            }
            System.out.println("└─────────────────────────────────────");
        }
    }

    public Warehouse addWarehouse(Warehouse warehouse) throws SQLException {
        String sql = "INSERT INTO warehouses (name, location, is_active) VALUES (?, ?, ?) RETURNING id";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouse.getName());
            pstmt.setString(2, warehouse.getLocation());
            pstmt.setBoolean(3, warehouse.isActive());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    warehouse.setId(rs.getInt("id"));
                    System.out.println("\n✓ Склад успешно добавлен с ID: " + warehouse.getId());
                    return warehouse;
                }
            }
        } catch (SQLException e) {
            System.err.println("\n❌ Ошибка при добавлении склада: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public Warehouse findWarehouseById(int id) throws SQLException {
        String sql = "SELECT * FROM warehouses WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractWarehouseFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public Warehouse findWarehouseByName(String name) throws SQLException {
        String sql = "SELECT * FROM warehouses WHERE name ILIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractWarehouseFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Warehouse> getAllWarehouses() throws SQLException {
        List<Warehouse> warehouses = new ArrayList<>();
        String sql = "SELECT * FROM warehouses ORDER BY id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                warehouses.add(extractWarehouseFromResultSet(rs));
            }
        }
        return warehouses;
    }

    public List<Warehouse> getActiveWarehouses() throws SQLException {
        List<Warehouse> warehouses = new ArrayList<>();
        String sql = "SELECT * FROM warehouses WHERE is_active = true ORDER BY name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                warehouses.add(extractWarehouseFromResultSet(rs));
            }
        }
        return warehouses;
    }

    public boolean updateWarehouse(Warehouse warehouse) throws SQLException {
        String sql = "UPDATE warehouses SET name = ?, location = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouse.getName());
            pstmt.setString(2, warehouse.getLocation());
            pstmt.setInt(3, warehouse.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("\n✓ Данные склада успешно обновлены");
                return true;
            } else {
                System.out.println("\n❌ Склад с ID " + warehouse.getId() + " не найден");
                return false;
            }
        }
    }

    public boolean deactivateWarehouse(int id) throws SQLException {
        String checkInventorySql = "SELECT COUNT(*) FROM inventory WHERE warehouse_id = ? AND (quantity > 0 OR reserved_quantity > 0)";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkInventorySql)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("\n❌ Невозможно деактивировать склад: на нем есть товары с ненулевыми остатками");
                    return false;
                }
            }
        }

        String sql = "UPDATE warehouses SET is_active = false WHERE id = ? AND is_active = true";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("\n✓ Склад успешно деактивирован");
                return true;
            } else {
                System.out.println("\n❌ Склад с ID " + id + " не найден или уже неактивен");
                return false;
            }
        }
    }

    public boolean activateWarehouse(int id) throws SQLException {
        String sql = "UPDATE warehouses SET is_active = true WHERE id = ? AND is_active = false";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("\n✓ Склад успешно активирован");
                return true;
            } else {
                System.out.println("\n❌ Склад с ID " + id + " не найден или уже активен");
                return false;
            }
        }
    }

    public void getWarehouseStatistics(int warehouseId) throws SQLException {
        String sql = "SELECT " +
                "COUNT(DISTINCT p.id) as total_products, " +
                "COALESCE(SUM(i.quantity), 0) as total_quantity, " +
                "COALESCE(SUM(i.reserved_quantity), 0) as total_reserved, " +
                "COALESCE(SUM(i.quantity - i.reserved_quantity), 0) as total_available " +
                "FROM warehouses w " +
                "LEFT JOIN inventory i ON w.id = i.warehouse_id " +
                "LEFT JOIN products p ON i.product_id = p.id " +
                "WHERE w.id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, warehouseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n╔═══════════════════════════════════════════╗");
                    System.out.println("║     СТАТИСТИКА СКЛАДА                     ║");
                    System.out.println("╠═══════════════════════════════════════════╣");
                    System.out.println("║ Всего видов товаров: " + String.format("%-20s", rs.getInt("total_products")) + " ║");
                    System.out.println("║ Общее количество:     " + String.format("%-20s", rs.getBigDecimal("total_quantity")) + "║");
                    System.out.println("║ Зарезервировано:      " + String.format("%-20s", rs.getBigDecimal("total_reserved")) + "║");
                    System.out.println("║ Доступно:             " + String.format("%-20s", rs.getBigDecimal("total_available")) + "║");
                    System.out.println("╚═══════════════════════════════════════════╝");
                }
            }
        }
    }

    private Warehouse extractWarehouseFromResultSet(ResultSet rs) throws SQLException {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(rs.getInt("id"));
        warehouse.setName(rs.getString("name"));
        warehouse.setLocation(rs.getString("location"));
        warehouse.setActive(rs.getBoolean("is_active"));

        try {
            warehouse.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException e) {}

        try {
            warehouse.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException e) {}

        return warehouse;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Соединение с БД закрыто");
        }
    }

    private void showMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       УПРАВЛЕНИЕ СКЛАДАМИ              ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║  1. Добавить склад                     ║");
        System.out.println("║  2. Найти склад по ID                  ║");
        System.out.println("║  3. Найти склад по названию            ║");
        System.out.println("║  4. Показать все склады                ║");
        System.out.println("║  5. Показать активные склады           ║");
        System.out.println("║  6. Редактировать склад                ║");
        System.out.println("║  7. Деактивировать склад               ║");
        System.out.println("║  8. Активировать склад                 ║");
        System.out.println("║  9. Статистика склада                  ║");
        System.out.println("║  0. Вернуться к выбору модуля          ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.print("➜ Выберите действие: ");
    }

    public void start(Scanner scanner) {
        boolean running = true;

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     СИСТЕМА УПРАВЛЕНИЯ СКЛАДОМ         ║");
        System.out.println("║     Модуль: Управление складами        ║");
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
                    System.out.println("\n--- ДОБАВЛЕНИЕ НОВОГО СКЛАДА ---");
                    System.out.print("Название склада: ");
                    String name = scanner.nextLine().trim();
                    if (name.isEmpty()) {
                        System.out.println("❌ Название не может быть пустым!");
                        break;
                    }

                    System.out.print("Адрес склада: ");
                    String location = scanner.nextLine().trim();

                    Warehouse newWarehouse = new Warehouse(name, location);
                    try {
                        Warehouse created = addWarehouse(newWarehouse);
                        if (created != null) {
                            created.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 2:
                    System.out.println("\n--- ПОИСК СКЛАДА ПО ID ---");
                    System.out.print("Введите ID склада: ");
                    try {
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            Warehouse found = findWarehouseById(id);
                            if (found != null) {
                                System.out.println("\n✅ Склад найден:");
                                found.display();
                            } else {
                                System.out.println("\n❌ Склад с ID " + id + " не найден");
                            }
                        } catch (SQLException e) {
                            System.err.println("Ошибка БД: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        System.out.println("\n❌ Ошибка: Введите корректный ID!");
                        scanner.nextLine();
                    }
                    break;

                case 3:
                    System.out.println("\n--- ПОИСК СКЛАДА ПО НАЗВАНИЮ ---");
                    System.out.print("Введите название склада: ");
                    String searchName = scanner.nextLine().trim();
                    if (searchName.isEmpty()) {
                        System.out.println("❌ Название не может быть пустым!");
                        break;
                    }
                    try {
                        Warehouse foundByName = findWarehouseByName(searchName);
                        if (foundByName != null) {
                            System.out.println("\n✅ Склад найден:");
                            foundByName.display();
                        } else {
                            System.out.println("\n❌ Склад с названием '" + searchName + "' не найден");
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 4:
                    System.out.println("\n--- ВСЕ СКЛАДЫ ---");
                    try {
                        List<Warehouse> all = getAllWarehouses();
                        if (all.isEmpty()) {
                            System.out.println("\n❌ Склады не найдены");
                        } else {
                            System.out.println("\n✅ Всего складов: " + all.size());
                            for (int i = 0; i < all.size(); i++) {
                                System.out.println("\n--- Склад " + (i + 1) + " ---");
                                all.get(i).display();
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("\n--- АКТИВНЫЕ СКЛАДЫ ---");
                    try {
                        List<Warehouse> active = getActiveWarehouses();
                        if (active.isEmpty()) {
                            System.out.println("\n❌ Активные склады не найдены");
                        } else {
                            System.out.println("\n✅ Активных складов: " + active.size());
                            for (int i = 0; i < active.size(); i++) {
                                System.out.println("\n--- Склад " + (i + 1) + " ---");
                                active.get(i).display();
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 6:
                    System.out.println("\n--- РЕДАКТИРОВАНИЕ СКЛАДА ---");
                    System.out.print("Введите ID склада для редактирования: ");
                    try {
                        int editId = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            Warehouse toEdit = findWarehouseById(editId);
                            if (toEdit != null) {
                                System.out.println("\nТекущие данные склада:");
                                toEdit.display();
                                System.out.println("\nВведите новые данные (оставьте пустым, чтобы не менять):");

                                System.out.print("Новое название [" + toEdit.getName() + "]: ");
                                String newName = scanner.nextLine().trim();
                                if (!newName.isEmpty()) toEdit.setName(newName);

                                System.out.print("Новый адрес [" + (toEdit.getLocation() != null ? toEdit.getLocation() : "-") + "]: ");
                                String newLocation = scanner.nextLine().trim();
                                if (!newLocation.isEmpty()) toEdit.setLocation(newLocation);

                                updateWarehouse(toEdit);
                                System.out.println("\nОбновленные данные:");
                                toEdit.display();
                            } else {
                                System.out.println("\n❌ Склад с ID " + editId + " не найден");
                            }
                        } catch (SQLException e) {
                            System.err.println("Ошибка БД: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        System.out.println("\n❌ Ошибка: Введите корректный ID!");
                        scanner.nextLine();
                    }
                    break;

                case 7:
                    System.out.println("\n--- ДЕАКТИВАЦИЯ СКЛАДА ---");
                    System.out.print("Введите ID склада для деактивации: ");
                    try {
                        int deactivateId = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            Warehouse toDeactivate = findWarehouseById(deactivateId);
                            if (toDeactivate != null) {
                                System.out.println("\nСклад, который будет деактивирован:");
                                toDeactivate.display();
                                System.out.print("\nВы уверены? (yes/no): ");
                                String confirm = scanner.nextLine().trim();
                                if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
                                    deactivateWarehouse(deactivateId);
                                } else {
                                    System.out.println("\n❌ Операция деактивации отменена");
                                }
                            } else {
                                System.out.println("\n❌ Склад с ID " + deactivateId + " не найден");
                            }
                        } catch (SQLException e) {
                            System.err.println("Ошибка БД: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        System.out.println("\n❌ Ошибка: Введите корректный ID!");
                        scanner.nextLine();
                    }
                    break;

                case 8:
                    System.out.println("\n--- АКТИВАЦИЯ СКЛАДА ---");
                    System.out.print("Введите ID склада для активации: ");
                    try {
                        int activateId = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            activateWarehouse(activateId);
                        } catch (SQLException e) {
                            System.err.println("Ошибка БД: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        System.out.println("\n❌ Ошибка: Введите корректный ID!");
                        scanner.nextLine();
                    }
                    break;

                case 9:
                    System.out.println("\n--- СТАТИСТИКА СКЛАДА ---");
                    System.out.print("Введите ID склада: ");
                    try {
                        int statId = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            Warehouse checkWarehouse = findWarehouseById(statId);
                            if (checkWarehouse != null) {
                                System.out.println("\nСклад: " + checkWarehouse.getName());
                                getWarehouseStatistics(statId);
                            } else {
                                System.out.println("\n❌ Склад с ID " + statId + " не найден");
                            }
                        } catch (SQLException e) {
                            System.err.println("Ошибка БД: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        System.out.println("\n❌ Ошибка: Введите корректный ID!");
                        scanner.nextLine();
                    }
                    break;

                case 0:
                    System.out.println("\n╔════════════════════════════════════════╗");
                    System.out.println("║     Возврат в главное меню...          ║");
                    System.out.println("╚════════════════════════════════════════╝");
                    running = false;
                    break;

                default:
                    System.out.println("\n❌ Неверный выбор! Пожалуйста, выберите действие от 0 до 9");
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