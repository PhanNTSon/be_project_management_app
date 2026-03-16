
CREATE TABLE ChangeRequest (
    ChangeRequestId INT IDENTITY PRIMARY KEY,

    ProjectId INT NOT NULL,
    RequesterId BIGINT NOT NULL,

    Title NVARCHAR(255) NOT NULL,
    [Description] NVARCHAR(MAX),

    [Status] VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK ([Status] IN ('PENDING','APPROVED','REJECTED')),

    ReviewedBy BIGINT NULL,
    ReviewedAt DATETIME2 NULL,

    CreatedAt DATETIME2 DEFAULT SYSDATETIME(),

    FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId) ON DELETE CASCADE,
    FOREIGN KEY (RequesterId) REFERENCES [User](UserId),
    FOREIGN KEY (ReviewedBy) REFERENCES [User](UserId)
);

CREATE TABLE ChangeItem (
    ChangeItemId INT IDENTITY PRIMARY KEY,
    ChangeRequestId INT NOT NULL,
    EntityType NVARCHAR (50) NOT NULL,
    EntityId INT NULL,
    Operation VARCHAR(10) NOT NULL CHECK (
        Operation IN ('CREATE', 'UPDATE', 'DELETE')
    ),
    FieldName NVARCHAR (100) NULL,
    OldValue NVARCHAR (MAX),
    NewValue NVARCHAR (MAX),
    CreatedAt DATETIME2 DEFAULT SYSDATETIME (),
    FOREIGN KEY (ChangeRequestId) REFERENCES ChangeRequest (ChangeRequestId) ON DELETE CASCADE
);

CREATE INDEX IDX_ChangeRequest_Project
ON ChangeRequest(ProjectId);

CREATE INDEX IDX_ChangeRequest_Status
ON ChangeRequest(Status);

CREATE INDEX IDX_ChangeItem_Request
ON ChangeItem(ChangeRequestId);

