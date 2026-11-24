INSERT INTO sys_user (id, username, password, nickname, phone, email, status) VALUES
  (1, 'admin', '$2a$10$Qnuh2UllMf52q/Q9B3NDmugxLnrL.6C2igfYCLVGueJl9JrMoYC4K', '系统管理员', '13800000000', 'admin@erp.local', 1);

INSERT INTO sys_role (id, role_code, role_name, description) VALUES
  (1, 'ROLE_ADMIN', '系统管理员', '拥有全部接口权限'),
  (2, 'ROLE_SERVICE', '服务账号', 'MCP 网关可调用的接口权限');

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO sys_permission (id, perm_code, perm_name, resource_type, path) VALUES
  (1,  'supplier:read',          '供应商查询',   'API', 'GET /api/suppliers/search'),
  (2,  'supplier:page',          '供应商分页',   'API', 'GET /api/suppliers/page'),
  (3,  'supplier:get',           '供应商详情',   'API', 'GET /api/suppliers/get/{id}'),
  (4,  'supplier:create',        '新增供应商',   'API', 'POST /api/suppliers/create'),
  (5,  'supplier:update',        '供应商状态更新', 'API', 'PATCH /api/suppliers/update-status/{id}'),
  (6,  'part:read',              '零件查询',     'API', 'GET /api/parts/search'),
  (7,  'part:page',              '零件分页',     'API', 'GET /api/parts/page'),
  (8,  'part:get',               '零件详情',     'API', 'GET /api/parts/get/{id}'),
  (9,  'part:by-supplier',       '按供应商查零件', 'API', 'GET /api/parts/supplier/{supplier_id}'),
  (10, 'part:create',            '新增零件',     'API', 'POST /api/parts/create'),
  (11, 'order:create',           '创建订单',     'API', 'POST /api/orders/create'),
  (12, 'order:update',           '更新订单',     'API', 'PUT /api/orders/update/{id}'),
  (13, 'order:page',             '订单分页',     'API', 'GET /api/orders/page'),
  (14, 'order:read',             '订单详情',     'API', 'GET /api/orders/get/{id}'),
  (15, 'order:search-details',   '订单明细查询', 'API', 'GET /api/orders/search-details'),
  (16, 'order:statistics',       '订单统计',     'API', 'GET /api/orders/statistics'),
  (17, 'order:update-status',    '订单状态更新', 'API', 'PATCH /api/orders/update-status/{id}'),
  (18, 'inventory:read',         '库存详情',     'API', 'GET /api/inventory/get/{id}'),
  (19, 'inventory:page',         '库存分页',     'API', 'GET /api/inventory/page'),
  (20, 'inventory:warning',      '库存预警',     'API', 'GET /api/inventory/warning'),
  (21, 'inventory:check',        '库存校验',     'API', 'GET /api/inventory/check'),
  (22, 'inventory:create',       '库存入库',     'API', 'POST /api/inventory/inbound'),
  (23, 'inventory:update',       '库存出库',     'API', 'POST /api/inventory/outbound');

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
  (1, 1),  (1, 2),  (1, 3),  (1, 4),  (1, 5),  (1, 6),  (1, 7),  (1, 8),
  (1, 9),  (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16),
  (1, 17), (1, 18), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23),
  (2, 1),  (2, 4),  (2, 6),  (2, 10), (2, 11), (2, 12), (2, 14), (2, 17),
  (2, 18), (2, 22), (2, 23);

INSERT INTO sys_client (id, client_id, client_secret_hash, client_name, scopes, status) VALUES
  (1, 'mcp-gateway', '', 'MCP 网关',
   'supplier:read,supplier:create,part:read,part:create,order:read,order:create,order:update,order:update-status,inventory:read,inventory:create,inventory:update',
   1);
