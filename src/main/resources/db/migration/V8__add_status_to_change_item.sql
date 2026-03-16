-- Add Status column to ChangeItem table for granular approval/rejection
ALTER TABLE ChangeItem
ADD [Status] NVARCHAR(20) NOT NULL DEFAULT 'PENDING';
