
USE transformatives_secure;

ALTER TABLE shipments
    ADD COLUMN recipient_phone VARCHAR(25) NULL,
    ADD COLUMN postal_code VARCHAR(20) NULL,
    ADD COLUMN package_type VARCHAR(30) NULL;
