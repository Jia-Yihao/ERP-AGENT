CREATE TABLE IF NOT EXISTS supplier (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplier_code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  contact_person VARCHAR(64),
  phone VARCHAR(32),
  email VARCHAR(128),
  address VARCHAR(256),
  credit_rating VARCHAR(8),
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS part (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  part_code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  model VARCHAR(128),
  specification VARCHAR(256),
  unit VARCHAR(16),
  purchase_price DECIMAL(12,2) NOT NULL,
  suggested_retail_price DECIMAL(12,2),
  stock_warning_value INT DEFAULT 0,
  supplier_id BIGINT,
  category VARCHAR(64),
  description VARCHAR(512),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
