import java.sql.SQLException;
import java.util.Scanner;

public class ApplicationMain {

    private static boolean globalRunning = true;

    private static void showMainMenu() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   СИСТЕМА УПРАВЛЕНИЯ СКЛАДОМ                   ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1.  Управление товарами (ProductManager)                      ║");
        System.out.println("║  2.  Управление складами (WarehouseManager)                    ║");
        System.out.println("║  3.  Управление поставщиками (SupplierManager)                 ║");
        System.out.println("║  4.  Приход товара (ReceiptManager)                            ║");
        System.out.println("║  5.  Расход товара (ShipmentManager)                           ║");
        System.out.println("║  6.  Резервирование товаров (ReservationManager)               ║");
        System.out.println("║  7.  Контроль остатков (StockControlManager)                   ║");
        System.out.println("║  0.  Выход                                                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("➜ Выберите модуль: ");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                ║");
        System.out.println("║                   ДОБРО ПОЖАЛОВАТЬ В СИСТЕМУ                   ║");
        System.out.println("║                       УПРАВЛЕНИЯ СКЛАДОМ                       ║");
        System.out.println("║                                                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        while (globalRunning) {
            showMainMenu();
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
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("                    ЗАПУСК МОДУЛЯ: Управление товарами");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    try {
                        ProductManager productManager = new ProductManager();
                        productManager.start(scanner);
                        productManager.close();
                    } catch (SQLException e) {
                        System.err.println("Ошибка подключения к БД: " + e.getMessage());
                    }
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║                   ВОЗВРАТ В ГЛАВНОЕ МЕНЮ                       ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    break;

                case 2:
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("                    ЗАПУСК МОДУЛЯ: Управление складами");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    try {
                        WarehouseManager warehouseManager = new WarehouseManager();
                        warehouseManager.start(scanner);
                        warehouseManager.close();
                    } catch (SQLException e) {
                        System.err.println("Ошибка подключения к БД: " + e.getMessage());
                    }
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║                   ВОЗВРАТ В ГЛАВНОЕ МЕНЮ                       ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    break;

                case 3:
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("                    ЗАПУСК МОДУЛЯ: Управление поставщиками");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    try {
                        SupplierManager supplierManager = new SupplierManager();
                        supplierManager.start(scanner);
                        supplierManager.close();
                    } catch (SQLException e) {
                        System.err.println("Ошибка подключения к БД: " + e.getMessage());
                    }
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║                   ВОЗВРАТ В ГЛАВНОЕ МЕНЮ                       ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    break;

                case 4:
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("                    ЗАПУСК МОДУЛЯ: Приход товара");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    try {
                        ReceiptManager receiptManager = new ReceiptManager();
                        receiptManager.start(scanner);
                        receiptManager.close();
                    } catch (SQLException e) {
                        System.err.println("Ошибка подключения к БД: " + e.getMessage());
                    }
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║                   ВОЗВРАТ В ГЛАВНОЕ МЕНЮ                       ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    break;

                case 5:
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("                    ЗАПУСК МОДУЛЯ: Расход товара");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    try {
                        ShipmentManager shipmentManager = new ShipmentManager();
                        shipmentManager.start(scanner);
                        shipmentManager.close();
                    } catch (SQLException e) {
                        System.err.println("Ошибка подключения к БД: " + e.getMessage());
                    }
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║                   ВОЗВРАТ В ГЛАВНОЕ МЕНЮ                       ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    break;

                case 6:
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("                    ЗАПУСК МОДУЛЯ: Резервирование товаров");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    try {
                        ReservationManager reservationManager = new ReservationManager();
                        reservationManager.start(scanner);
                        reservationManager.close();
                    } catch (SQLException e) {
                        System.err.println("Ошибка подключения к БД: " + e.getMessage());
                    }
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║                   ВОЗВРАТ В ГЛАВНОЕ МЕНЮ                       ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    break;

                case 7:
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("                    ЗАПУСК МОДУЛЯ: Контроль остатков");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    try {
                        StockControlManager stockControlManager = new StockControlManager();
                        stockControlManager.start(scanner);
                        stockControlManager.close();
                    } catch (SQLException e) {
                        System.err.println("Ошибка подключения к БД: " + e.getMessage());
                    }
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║                   ВОЗВРАТ В ГЛАВНОЕ МЕНЮ                       ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    break;

                case 0:
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║                                                                ║");
                    System.out.println("║                     Выход из программы...                      ║");
                    System.out.println("║                         До свидания!                           ║");
                    System.out.println("║                                                                ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    globalRunning = false;
                    break;

                default:
                    System.out.println("\n❌ Неверный выбор! Пожалуйста, выберите действие от 0 до 7");
                    break;
            }
        }

        scanner.close();
    }
}