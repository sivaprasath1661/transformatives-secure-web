
USE transformatives_secure;

-- Existing shipments remain unassigned until an admin
-- assigns a courier.

ALTER TABLE shipments
ADD COLUMN assigned_courier VARCHAR(100) NULL;

CREATE INDEX idx_shipments_assigned_courier
ON shipments (assigned_courier);
