DELETE FROM purchase_order_detail;
DELETE FROM purchase_order;

INSERT INTO purchase_order (id, order_number, total_amount, status, remark, order_time) VALUES
(1, 'PO20261112001', 3500.00, 1, '示例已审核订单', CURRENT_TIMESTAMP),
(2, 'PO20261112002', 860.00, 0, '示例待审核订单', CURRENT_TIMESTAMP);

INSERT INTO purchase_order_detail (id, order_id, part_id, quantity, unit_price, remark) VALUES
(1, 1, 1, 100, 25.50, '火花塞批量'),
(2, 1, 2, 10, 95.00, '刹车片'),
(3, 2, 6, 1, 860.00, '传动轴');
