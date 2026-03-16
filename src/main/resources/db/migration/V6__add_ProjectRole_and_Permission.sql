CREATE TABLE ProjectRole (
    ProjectRoleId INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL UNIQUE,
    Description NVARCHAR(255),
    CreatedAt DATETIME2 DEFAULT GETUTCDATE()
);

CREATE TABLE Permission (
    PermissionId INT IDENTITY(1,1) PRIMARY KEY,
    Code NVARCHAR(100) NOT NULL UNIQUE,
    [Description] NVARCHAR(255),
    CreatedAt DATETIME2 DEFAULT GETUTCDATE()
);

CREATE TABLE ProjectRolePermission (
    ProjectRoleId INT NOT NULL,
    PermissionId INT NOT NULL,

    PRIMARY KEY (ProjectRoleId, PermissionId),

    FOREIGN KEY (ProjectRoleId) REFERENCES ProjectRole(ProjectRoleId),
    FOREIGN KEY (PermissionId) REFERENCES Permission(PermissionId)
);

ALTER TABLE ProjectMember
DROP COLUMN Role;

ALTER TABLE ProjectMember
ADD ProjectRoleId INT NOT NULL;

ALTER TABLE ProjectMember
ADD CONSTRAINT FK_ProjectMember_ProjectRole
FOREIGN KEY (ProjectRoleId)
REFERENCES ProjectRole(ProjectRoleId);

