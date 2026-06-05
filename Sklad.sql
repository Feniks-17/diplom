-- ============================================================
-- Таблица: товары (products)
-- ============================================================
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,            -- артикул
    name VARCHAR(255) NOT NULL,                 -- название товара
    description TEXT,                           -- описание
    category VARCHAR(100),                      -- категория
    unit VARCHAR(20) DEFAULT 'шт',              -- единица измерения
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Таблица: склады (warehouses)
-- ============================================================
CREATE TABLE IF NOT EXISTS warehouses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,                 -- название склада
    location VARCHAR(255),                      -- адрес
    is_active BOOLEAN DEFAULT TRUE
);

-- ============================================================
-- Таблица: поставщики (suppliers)
-- ============================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE
);

-- ============================================================
-- Таблица: остатки на складе (inventory) - упрощённо, без привязки к ячейкам
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory (
    id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    warehouse_id INTEGER NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    quantity NUMERIC(12,3) NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reserved_quantity NUMERIC(12,3) NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, warehouse_id)  -- один товар на одном складе – одна запись
);

-- ============================================================
-- Таблица: приходные накладные (receipts)
-- ============================================================
CREATE TABLE IF NOT EXISTS receipts (
    id SERIAL PRIMARY KEY,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id INTEGER REFERENCES suppliers(id),
    warehouse_id INTEGER NOT NULL REFERENCES warehouses(id),
    receipt_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(20) DEFAULT 'draft',         -- draft, posted, cancelled
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Таблица: строки прихода (receipt_items) - без привязки к ячейкам
-- ============================================================
CREATE TABLE IF NOT EXISTS receipt_items (
    id SERIAL PRIMARY KEY,
    receipt_id INTEGER NOT NULL REFERENCES receipts(id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(id),
    quantity NUMERIC(12,3) NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(15,2) NOT NULL,          -- цена закупки
    total_amount NUMERIC(15,2) GENERATED ALWAYS AS (quantity * unit_price) STORED
);

-- ============================================================
-- Таблица: расходные накладные (shipments)
-- ============================================================
CREATE TABLE IF NOT EXISTS shipments (
    id SERIAL PRIMARY KEY,
    shipment_number VARCHAR(50) NOT NULL UNIQUE,
    warehouse_id INTEGER NOT NULL REFERENCES warehouses(id),
    shipment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    customer_name VARCHAR(255),
    status VARCHAR(20) DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Таблица: строки расхода (shipment_items)
-- ============================================================
CREATE TABLE IF NOT EXISTS shipment_items (
    id SERIAL PRIMARY KEY,
    shipment_id INTEGER NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(id),
    quantity NUMERIC(12,3) NOT NULL CHECK (quantity > 0)
);

-- ============================================================
-- Индексы для ускорения запросов (упрощённые)
-- ============================================================
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category);

CREATE INDEX idx_inventory_product ON inventory(product_id);
CREATE INDEX idx_inventory_warehouse ON inventory(warehouse_id);

CREATE INDEX idx_receipts_date ON receipts(receipt_date);
CREATE INDEX idx_receipts_supplier ON receipts(supplier_id);

CREATE INDEX idx_shipments_date ON shipments(shipment_date);

CREATE INDEX idx_receipt_items_receipt ON receipt_items(receipt_id);
CREATE INDEX idx_receipt_items_product ON receipt_items(product_id);

CREATE INDEX idx_shipment_items_shipment ON shipment_items(shipment_id);
CREATE INDEX idx_shipment_items_product ON shipment_items(product_id);

-- ============================================================
-- Представление: текущие остатки по товарам
-- ============================================================
CREATE OR REPLACE VIEW current_stock AS
SELECT 
    p.id AS product_id,
    p.sku,
    p.name,
    p.category,
    w.id AS warehouse_id,
    w.name AS warehouse_name,
    COALESCE(i.quantity, 0) AS quantity,
    COALESCE(i.reserved_quantity, 0) AS reserved_quantity,
    COALESCE(i.quantity - i.reserved_quantity, 0) AS available_quantity
FROM products p
CROSS JOIN warehouses w
LEFT JOIN inventory i ON p.id = i.product_id AND w.id = i.warehouse_id
ORDER BY w.name, p.name;

-- ============================================================
-- Представление: сводные остатки (суммарно по товару)
-- ============================================================
CREATE OR REPLACE VIEW total_stock AS
SELECT 
    p.id AS product_id,
    p.sku,
    p.name,
    p.category,
    COALESCE(SUM(i.quantity), 0) AS total_quantity,
    COALESCE(SUM(i.reserved_quantity), 0) AS total_reserved,
    COALESCE(SUM(i.quantity - i.reserved_quantity), 0) AS total_available
FROM products p
LEFT JOIN inventory i ON p.id = i.product_id
GROUP BY p.id, p.sku, p.name, p.category
ORDER BY p.name;

-- ============================================================
-- Вставка тестовых данных
-- ============================================================
INSERT INTO warehouses (name, location) VALUES 
('Основной склад', 'Москва, ул. Складская 1'),
('Резервный склад', 'Москва, ул. Запасная 5');

INSERT INTO suppliers (name, contact_person, phone, email) VALUES 
('ООО "Поставка+"', 'Сергеев С.С.', '+7-999-123-4567', 'info@postavka.ru'),
('ИП Иванов', 'Иванов И.И.', '+7-999-765-4321', 'ivanov@mail.ru');

INSERT INTO products (sku, name, category, unit) VALUES 
('TV-001', 'Телевизор 43"', 'Электроника', 'шт'),
('TV-002', 'Телевизор 55"', 'Электроника', 'шт'),
('FR-001', 'Холодильник двухкамерный', 'Бытовая техника', 'шт');

-- Добавляем начальные остатки
INSERT INTO inventory (product_id, warehouse_id, quantity, reserved_quantity) VALUES 
(1, 1, 100, 10),
(2, 1, 50, 5),
(3, 1, 25, 2),
(1, 2, 30, 0);