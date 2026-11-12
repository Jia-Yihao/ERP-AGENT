CREATE TABLE IF NOT EXISTS inventory (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  part_id BIGINT NOT NULL UNIQUE,
  current_quantity INT NOT NULL DEFAULT 0,
  safety_stock INT NOT NULL DEFAULT 0,
  warehouse_location VARCHAR(128),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  part_id BIGINT NOT NULL,
  change_type VARCHAR(16) NOT NULL,
  quantity INT NOT NULL,
  before_quantity INT,
  after_quantity INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
