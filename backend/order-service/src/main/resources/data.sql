DELETE FROM outbox_event;
DELETE FROM order_status_log;
DELETE FROM purchase_receive;
DELETE FROM requisition_approval;
DELETE FROM purchase_requisition;
DELETE FROM purchase_order_detail;
DELETE FROM purchase_order;

INSERT INTO purchase_order (id, order_number, total_amount, status, remark, order_time) VALUES
(1, 'PO20261112001', 3500.00, 1, '示例已审核订单', CURRENT_TIMESTAMP),
(2, 'PO20261112002', 860.00, 0, '示例待审核订单', CURRENT_TIMESTAMP);

INSERT INTO purchase_order_detail (id, order_id, part_id, quantity, unit_price, remark) VALUES
(1, 1, 1, 100, 25.50, '火花塞批量'),
(2, 1, 2, 10, 95.00, '刹车片'),
(3, 2, 6, 1, 860.00, '传动轴');

INSERT INTO purchase_requisition (id, requisition_no, part_id, quantity, reason, applicant, status, approver, approved_at) VALUES
(1, 'PR20261120001', 1, 50, '火花塞安全库存补货', '张工', 1, NULL, NULL),
(2, 'PR20261122001', 6, 2, '传动轴备件采购', '王工', 0, NULL, NULL);

INSERT INTO order_status_log (id, order_id, from_status, to_status, operator, remark) VALUES
(1, 1, NULL, 0, 'system', '订单创建'),
(2, 1, 0, 1, '李工', '订单审核通过'),
(3, 2, NULL, 0, 'system', '订单创建');

INSERT INTO purchase_receive (id, receive_no, order_id, part_id, quantity, receiver, received_at, status) VALUES
(1, 'RC20261121001', 1, 1, 30, '赵收货', '2026-11-21 09:30:00', 1),
(2, 'RC20261123001', 1, 2, 5, '赵收货', '2026-11-23 14:00:00', 1);

INSERT INTO outbox_event (id, aggregate_type, aggregate_id, event_type, payload, status, retry_count, next_retry_at) VALUES
(1, 'PURCHASE_RECEIVE', '1', 'STOCK_INBOUND', '{"receiveId":1,"orderId":1,"partId":1,"quantity":30}', 0, 0, '2026-11-24 00:00:00');
