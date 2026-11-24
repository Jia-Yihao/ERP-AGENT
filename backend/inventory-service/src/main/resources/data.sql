INSERT INTO part_snapshot (id, name) VALUES
(1, '火花塞'),
(2, '机油滤清器'),
(3, '空气滤清器'),
(4, '刹车片'),
(5, '刹车盘'),
(6, '雨刮片'),
(7, '蓄电池'),
(8, '轮毂轴承');

DELETE FROM inventory_log;
DELETE FROM inventory;

INSERT INTO inventory (id, part_id, current_quantity, safety_stock, warehouse_location) VALUES
(1, 1, 120, 50, 'A-01'),
(2, 2, 25, 30, 'A-02'),
(3, 3, 60, 20, 'B-01'),
(4, 4, 200, 80, 'B-02'),
(5, 5, 180, 80, 'B-03'),
(6, 6, 5, 10, 'C-01'),
(7, 7, 40, 10, 'C-02'),
(8, 8, 300, 100, 'C-03');
