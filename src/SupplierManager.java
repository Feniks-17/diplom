import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SupplierManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Sklad";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Vvedensky2004";

    private Connection connection;

    public SupplierManager() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static class Supplier {
        private int id;
        private String name;
        private String contactPerson;
        private String phone;
        private String email;
        private boolean isActive;
        private Timestamp createdAt;
        private Timestamp updatedAt;

        public Supplier() {}

        public Supplier(String name, String contactPerson, String phone, String email) {
            this.name = name;
            this.contactPerson = contactPerson;
            this.phone = phone;
            this.email = email;
            this.isActive = true;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getContactPerson() { return contactPerson; }
        public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
        public Timestamp getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

        public void display() {
            System.out.println("┌─────────────────────────────────────────────");
            System.out.println("│ ID поставщика: " + id);
            System.out.println("│ Название: " + name);
            System.out.println("│ Контактное лицо: " + (contactPerson != null ? contactPerson : "-"));
            System.out.println("│ Телефон: " + (phone != null ? phone : "-"));
            System.out.println("│ Email: " + (email != null ? email : "-"));
            System.out.println("│ Статус: " + (isActive ? "✅ Активен" : "❌ Неактивен"));
            if (createdAt != null) {
                System.out.println("│ Создан: " + createdAt);
            }
            if (updatedAt != null) {
                System.out.println("│ Обновлен: " + updatedAt);
            }
            System.out.println("└─────────────────────────────────────────────");
        }
    }

    public Supplier addSupplier(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, is_active) VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactPerson());
            pstmt.setString(3, supplier.getPhone());
            pstmt.setString(4, supplier.getEmail());
            pstmt.setBoolean(5, supplier.isActive());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    supplier.setId(rs.getInt("id"));
                    System.out.println("\n✓ Поставщик успешно добавлен с ID: " + supplier.getId());
                    return supplier;
                }
            }
        } catch (SQLException e) {
            System.err.println("\n❌ Ошибка при добавлении поставщика: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public Supplier findSupplierById(int id) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractSupplierFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Supplier> findSuppliersByName(String namePattern) throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE name ILIKE ? ORDER BY name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + namePattern + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(extractSupplierFromResultSet(rs));
                }
            }
        }
        return suppliers;
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(extractSupplierFromResultSet(rs));
            }
        }
        return suppliers;
    }

    public List<Supplier> getActiveSuppliers() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE is_active = true ORDER BY name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                suppliers.add(extractSupplierFromResultSet(rs));
            }
        }
        return suppliers;
    }

    public boolean updateSupplier(Supplier supplier) throws SQLException {
        String sql = "UPDATE suppliers SET name = ?, contact_person = ?, phone = ?, email = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactPerson());
            pstmt.setString(3, supplier.getPhone());
            pstmt.setString(4, supplier.getEmail());
            pstmt.setInt(5, supplier.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("\n✓ Данные поставщика успешно обновлены");
                return true;
            } else {
                System.out.println("\n❌ Поставщик с ID " + supplier.getId() + " не найден");
                return false;
            }
        }
    }

    public boolean deactivateSupplier(int id) throws SQLException {
        String checkReceiptsSql = "SELECT COUNT(*) FROM receipts WHERE supplier_id = ? AND status != 'cancelled'";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkReceiptsSql)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("\n⚠️  ВНИМАНИЕ: У поставщика есть приходные накладные!");
                    System.out.print("Вы уверены, что хотите деактивировать поставщика? (yes/no): ");
                    Scanner scanner = new Scanner(System.in);
                    String confirm = scanner.nextLine().trim();
                    if (!confirm.equalsIgnoreCase("yes") && !confirm.equalsIgnoreCase("y")) {
                        System.out.println("❌ Операция деактивации отменена");
                        return false;
                    }
                }
            }
        }

        String sql = "UPDATE suppliers SET is_active = false WHERE id = ? AND is_active = true";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("\n✓ Поставщик успешно деактивирован");
                return true;
            } else {
                System.out.println("\n❌ Поставщик с ID " + id + " не найден или уже неактивен");
                return false;
            }
        }
    }

    public boolean activateSupplier(int id) throws SQLException {
        String sql = "UPDATE suppliers SET is_active = true WHERE id = ? AND is_active = false";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("\n✓ Поставщик успешно активирован");
                return true;
            } else {
                System.out.println("\n❌ Поставщик с ID " + id + " не найден или уже активен");
                return false;
            }
        }
    }

    public void getSupplierStatistics(int supplierId) throws SQLException {
        String sql = "SELECT " +
                "COUNT(DISTINCT r.id) as total_receipts, " +
                "COUNT(DISTINCT ri.product_id) as total_products, " +
                "COALESCE(SUM(ri.quantity), 0) as total_quantity, " +
                "COALESCE(SUM(ri.total_amount), 0) as total_amount " +
                "FROM suppliers s " +
                "LEFT JOIN receipts r ON s.id = r.supplier_id AND r.status = 'posted' " +
                "LEFT JOIN receipt_items ri ON r.id = ri.receipt_id " +
                "WHERE s.id = ? " +
                "GROUP BY s.id";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n╔═══════════════════════════════════════════╗");
                    System.out.println("║        СТАТИСТИКА ПОСТАВЩИКА                  ║");
                    System.out.println("╠═══════════════════════════════════════════╣");
                    System.out.println("║ Всего накладных:     " + String.format("%-25s", rs.getInt("total_receipts")) + "║");
                    System.out.println("║ Видов товаров:       " + String.format("%-25s", rs.getInt("total_products")) + "║");
                    System.out.println("║ Общее количество:    " + String.format("%-25s", rs.getBigDecimal("total_quantity")) + "║");
                    System.out.println("║ Общая сумма:         " + String.format("%-25s", rs.getBigDecimal("total_amount") + " руб.") + "║");
                    System.out.println("╚═══════════════════════════════════════════╝");
                }
            }
        }
    }

    private Supplier extractSupplierFromResultSet(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setId(rs.getInt("id"));
        supplier.setName(rs.getString("name"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setEmail(rs.getString("email"));
        supplier.setActive(rs.getBoolean("is_active"));

        try {
            supplier.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException e) {}

        try {
            supplier.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException e) {}

        return supplier;
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
        System.out.println("║         УПРАВЛЕНИЕ ПОСТАВЩИКАМИ            ║");
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.println("║  1.  Добавить поставщика                   ║");
        System.out.println("║  2.  Найти поставщика по ID                ║");
        System.out.println("║  3.  Найти поставщиков по названию         ║");
        System.out.println("║  4.  Показать всех поставщиков             ║");
        System.out.println("║  5.  Показать активных поставщиков         ║");
        System.out.println("║  6.  Редактировать поставщика              ║");
        System.out.println("║  7.  Деактивировать поставщика             ║");
        System.out.println("║  8.  Активировать поставщика               ║");
        System.out.println("║  9.  Статистика поставщика                 ║");
        System.out.println("║  0.  Вернуться к выбору модуля             ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.print("➜ Выберите действие: ");
    }

    public void start(Scanner scanner) {
        boolean running = true;

        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║       СИСТЕМА УПРАВЛЕНИЯ СКЛАДОМ           ║");
        System.out.println("║       Модуль: Управление поставщиками      ║");
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
                    System.out.println("\n--- ДОБАВЛЕНИЕ НОВОГО ПОСТАВЩИКА ---");
                    System.out.print("Название организации: ");
                    String name = scanner.nextLine().trim();
                    if (name.isEmpty()) {
                        System.out.println("❌ Название не может быть пустым!");
                        break;
                    }

                    System.out.print("Контактное лицо: ");
                    String contactPerson = scanner.nextLine().trim();

                    System.out.print("Телефон: ");
                    String phone = scanner.nextLine().trim();

                    System.out.print("Email: ");
                    String email = scanner.nextLine().trim();

                    if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        System.out.println("⚠️  Внимание: Email может быть некорректным");
                    }

                    Supplier newSupplier = new Supplier(name, contactPerson, phone, email);
                    try {
                        Supplier created = addSupplier(newSupplier);
                        if (created != null) {
                            created.display();
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 2:
                    System.out.println("\n--- ПОИСК ПОСТАВЩИКА ПО ID ---");
                    System.out.print("Введите ID поставщика: ");
                    try {
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            Supplier found = findSupplierById(id);
                            if (found != null) {
                                System.out.println("\n✅ Поставщик найден:");
                                found.display();
                            } else {
                                System.out.println("\n❌ Поставщик с ID " + id + " не найден");
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
                    System.out.println("\n--- ПОИСК ПОСТАВЩИКОВ ПО НАЗВАНИЮ ---");
                    System.out.print("Введите фрагмент названия: ");
                    String namePattern = scanner.nextLine().trim();
                    if (namePattern.isEmpty()) {
                        System.out.println("❌ Введите название для поиска!");
                        break;
                    }
                    try {
                        List<Supplier> byName = findSuppliersByName(namePattern);
                        if (byName.isEmpty()) {
                            System.out.println("\n❌ Поставщики не найдены");
                        } else {
                            System.out.println("\n✅ Найдено поставщиков: " + byName.size());
                            for (int i = 0; i < byName.size(); i++) {
                                System.out.println("\n--- Поставщик " + (i + 1) + " ---");
                                byName.get(i).display();
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 4:
                    System.out.println("\n--- ВСЕ ПОСТАВЩИКИ ---");
                    try {
                        List<Supplier> all = getAllSuppliers();
                        if (all.isEmpty()) {
                            System.out.println("\n❌ Поставщики не найдены");
                        } else {
                            System.out.println("\n✅ Всего поставщиков: " + all.size());
                            for (int i = 0; i < all.size(); i++) {
                                System.out.println("\n--- Поставщик " + (i + 1) + " ---");
                                all.get(i).display();
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("\n--- АКТИВНЫЕ ПОСТАВЩИКИ ---");
                    try {
                        List<Supplier> active = getActiveSuppliers();
                        if (active.isEmpty()) {
                            System.out.println("\n❌ Активные поставщики не найдены");
                        } else {
                            System.out.println("\n✅ Активных поставщиков: " + active.size());
                            for (int i = 0; i < active.size(); i++) {
                                System.out.println("\n--- Поставщик " + (i + 1) + " ---");
                                active.get(i).display();
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка БД: " + e.getMessage());
                    }
                    break;

                case 6:
                    System.out.println("\n--- РЕДАКТИРОВАНИЕ ПОСТАВЩИКА ---");
                    System.out.print("Введите ID поставщика для редактирования: ");
                    try {
                        int editId = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            Supplier toEdit = findSupplierById(editId);
                            if (toEdit != null) {
                                System.out.println("\nТекущие данные поставщика:");
                                toEdit.display();
                                System.out.println("\nВведите новые данные (оставьте пустым, чтобы не менять):");

                                System.out.print("Новое название [" + toEdit.getName() + "]: ");
                                String newName = scanner.nextLine().trim();
                                if (!newName.isEmpty()) toEdit.setName(newName);

                                System.out.print("Новое контактное лицо [" + (toEdit.getContactPerson() != null ? toEdit.getContactPerson() : "-") + "]: ");
                                String newContact = scanner.nextLine().trim();
                                if (!newContact.isEmpty()) toEdit.setContactPerson(newContact);

                                System.out.print("Новый телефон [" + (toEdit.getPhone() != null ? toEdit.getPhone() : "-") + "]: ");
                                String newPhone = scanner.nextLine().trim();
                                if (!newPhone.isEmpty()) toEdit.setPhone(newPhone);

                                System.out.print("Новый email [" + (toEdit.getEmail() != null ? toEdit.getEmail() : "-") + "]: ");
                                String newEmail = scanner.nextLine().trim();
                                if (!newEmail.isEmpty()) toEdit.setEmail(newEmail);

                                updateSupplier(toEdit);
                                System.out.println("\nОбновленные данные:");
                                toEdit.display();
                            } else {
                                System.out.println("\n❌ Поставщик с ID " + editId + " не найден");
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
                    System.out.println("\n--- ДЕАКТИВАЦИЯ ПОСТАВЩИКА ---");
                    System.out.print("Введите ID поставщика для деактивации: ");
                    try {
                        int deactivateId = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            Supplier toDeactivate = findSupplierById(deactivateId);
                            if (toDeactivate != null) {
                                System.out.println("\nПоставщик, который будет деактивирован:");
                                toDeactivate.display();
                                deactivateSupplier(deactivateId);
                            } else {
                                System.out.println("\n❌ Поставщик с ID " + deactivateId + " не найден");
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
                    System.out.println("\n--- АКТИВАЦИЯ ПОСТАВЩИКА ---");
                    System.out.print("Введите ID поставщика для активации: ");
                    try {
                        int activateId = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            activateSupplier(activateId);
                        } catch (SQLException e) {
                            System.err.println("Ошибка БД: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        System.out.println("\n❌ Ошибка: Введите корректный ID!");
                        scanner.nextLine();
                    }
                    break;

                case 9:
                    System.out.println("\n--- СТАТИСТИКА ПОСТАВЩИКА ---");
                    System.out.print("Введите ID поставщика: ");
                    try {
                        int statId = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            Supplier checkSupplier = findSupplierById(statId);
                            if (checkSupplier != null) {
                                System.out.println("\nПоставщик: " + checkSupplier.getName());
                                getSupplierStatistics(statId);
                            } else {
                                System.out.println("\n❌ Поставщик с ID " + statId + " не найден");
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
                    System.out.println("\n╔════════════════════════════════════════════╗");
                    System.out.println("║         Возврат в главное меню...          ║");
                    System.out.println("╚════════════════════════════════════════════╝");
                    running = false;
                    break;

                default:
                    System.out.println("\n❌ Неверный выбор! Пожалуйста, выберите действие от 0 до 9");
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