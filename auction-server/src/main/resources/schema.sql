-- ==============================================================
-- KHỞI TẠO DATABASE - Tạo các bảng cho hệ thống đấu giá
-- ==============================================================
-- File này tự động chạy mỗi khi server khởi động.
-- CREATE TABLE IF NOT EXISTS = chỉ tạo bảng nếu chưa tồn tại.
-- ==============================================================

-- Bảng người dùng
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,         -- UUID duy nhất
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,     -- Mật khẩu đã mã hoá (hash)
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL,          -- BIDDER, SELLER, ADMIN
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng sản phẩm đấu giá
CREATE TABLE IF NOT EXISTS items (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(50),                    -- ELECTRONICS, ART, VEHICLE
    seller_id VARCHAR(36) NOT NULL,      -- Ai đăng bán
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id)
);

-- Bảng phiên đấu giá
CREATE TABLE IF NOT EXISTS auctions (
    id VARCHAR(36) PRIMARY KEY,
    item_id VARCHAR(36) NOT NULL,        -- Sản phẩm được đấu giá
    starting_price DECIMAL(15,2) NOT NULL,  -- Giá khởi điểm
    current_price DECIMAL(15,2),         -- Giá hiện tại (cao nhất)
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',   -- OPEN, RUNNING, FINISHED, CANCELED
    winner_id VARCHAR(36),               -- Người thắng (null nếu chưa kết thúc)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (winner_id) REFERENCES users(id)
);

-- Bảng lịch sử đặt giá
CREATE TABLE IF NOT EXISTS bid_transactions (
    id VARCHAR(36) PRIMARY KEY,
    auction_id VARCHAR(36) NOT NULL,
    bidder_id VARCHAR(36) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,       -- Số tiền đặt giá
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES users(id)
);

-- Bảng auto-bid (đặt giá tự động)
CREATE TABLE IF NOT EXISTS auto_bids (
    id VARCHAR(36) PRIMARY KEY,
    auction_id VARCHAR(36) NOT NULL,
    bidder_id VARCHAR(36) NOT NULL,
    max_amount DECIMAL(15,2) NOT NULL,   -- Giá tối đa sẵn sàng trả
    increment DECIMAL(15,2) NOT NULL,    -- Bước nhảy mỗi lần tăng giá
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES users(id)
);
