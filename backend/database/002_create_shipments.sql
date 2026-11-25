
CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tracking_number VARCHAR(24) NOT NULL,
    owner_username VARCHAR(100) NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    destination_city VARCHAR(100) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_shipments_tracking UNIQUE (tracking_number),
    CONSTRAINT fk_shipments_owner
        FOREIGN KEY (owner_username)
        REFERENCES app_users(username)
);
