DELETE FROM part;
DELETE FROM supplier;

INSERT INTO supplier (id, supplier_code, name, contact_person, phone, email, address, credit_rating, status) VALUES
(1, 'SUP001', '一汽大众', '张工', '0431-88880001', 'faw-vw@example.com', '吉林长春', 'A', 1),
(2, 'SUP002', '上汽大众', '李工', '021-88880002', 'saic-vw@example.com', '上海安亭', 'A', 1),
(3, 'SUP003', '博世', '王工', '021-88880003', 'bosch@example.com', '上海', 'A', 1),
(4, 'SUP004', '采埃孚', '赵工', '021-88880004', 'zf@example.com', '上海', 'A', 1),
(5, 'SUP005', '大陆集团', '陈工', '021-88880005', 'continental@example.com', '上海', 'B', 1),
(6, 'SUP006', '华域汽车', '刘工', '021-88880006', 'huayu@example.com', '上海', 'B', 1);

INSERT INTO part (id, part_code, name, model, specification, unit, purchase_price, suggested_retail_price, stock_warning_value, supplier_id, category, description) VALUES
(1, 'PART001', '火花塞', 'NGK-BKR6E', '标准型铱金', '个', 25.50, 45.00, 50, 3, '发动机系统', '四缸发动机点火件'),
(2, 'PART002', '刹车片', 'BP-FRONT-01', '前桥陶瓷片', '套', 150.00, 268.00, 30, 3, '制动系统', '前制动摩擦片总成'),
(3, 'PART003', '制动盘', 'DISC-300', '通风盘 300mm', '件', 320.00, 520.00, 20, 4, '制动系统', '前通风制动盘'),
(4, 'PART004', '机油滤芯', 'OF-2026', '旋装式', '个', 18.00, 35.00, 80, 5, '发动机系统', '机油滤清器'),
(5, 'PART005', '空气滤芯', 'AF-2026', '平板式', '个', 22.00, 42.00, 80, 5, '发动机系统', '空气滤清器'),
(6, 'PART006', '传动轴', 'SHAFT-A4', '等速万向节', '根', 860.00, 1380.00, 10, 4, '传动系统', '前驱半轴总成'),
(7, 'PART007', '发电机', 'ALT-120A', '120A', '台', 720.00, 1180.00, 10, 6, '电气系统', '交流发电机'),
(8, 'PART008', '油封', 'SEAL-4050', '40x50x7', '个', 8.50, 18.00, 100, 5, '密封件/橡胶件', '骨架油封');
