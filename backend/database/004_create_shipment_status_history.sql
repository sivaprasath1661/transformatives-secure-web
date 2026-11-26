
USE transformatives_secure;

CREATE TABLE IF NOT EXISTS shipment_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tracking_number VARCHAR(24) NOT NULL,
    old_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    changed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_history_tracking (tracking_number)
);

