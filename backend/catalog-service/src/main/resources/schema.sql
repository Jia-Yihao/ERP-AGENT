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
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64),
  update_by VARCHAR(64),
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0
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
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64),
  update_by VARCHAR(64),
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS supplier_quote (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplier_id BIGINT NOT NULL,
  part_id BIGINT NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  lead_days INT DEFAULT 0,
  valid_from DATE,
  valid_to DATE,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64),
  update_by VARCHAR(64),
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS supplier_performance (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplier_id BIGINT NOT NULL,
  stat_date DATE NOT NULL,
  on_time_rate DECIMAL(6,4) DEFAULT 0,
  quality_rate DECIMAL(6,4) DEFAULT 0,
  avg_lead_days DECIMAL(10,2) DEFAULT 0,
  score DECIMAL(6,4) DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64),
  update_by VARCHAR(64),
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0
);

