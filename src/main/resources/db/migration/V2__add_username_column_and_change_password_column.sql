-- Đổi tên cột PasswordHash -> Password
EXEC sp_rename 'User.PasswordHash', 'Password', 'COLUMN';

-- Thêm cột Username
ALTER TABLE [User]
ADD Username NVARCHAR(255) NOT NULL UNIQUE;