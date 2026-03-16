-- Active: 1759823412677@@127.0.0.1@1433@master
--- USER & ROLE
CREATE TABLE [User] (
    UserId BIGINT IDENTITY PRIMARY KEY,
    Email NVARCHAR(255) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(500) NOT NULL,
    FullName NVARCHAR(255),
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE [Role] (
    RoleId BIGINT IDENTITY PRIMARY KEY,
    RoleName NVARCHAR(100) NOT NULL UNIQUE,
    Description NVARCHAR(500)
);
CREATE TABLE UserRole (
    UserId BIGINT NOT NULL,
    RoleId BIGINT NOT NULL,
    PRIMARY KEY (UserId, RoleId),
    FOREIGN KEY (UserId) REFERENCES [User](UserId),
    FOREIGN KEY (RoleId) REFERENCES Role(RoleId)
);

--- PROJECT DOMAIN
CREATE TABLE Project (
    ProjectId INT IDENTITY PRIMARY KEY,
    ProjectName NVARCHAR(255) NOT NULL,
    Description NVARCHAR(MAX),
    ContextDiagramUrl TEXT,
    OwnerId BIGINT NOT NULL,
    TemplateId INT,
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (OwnerId) REFERENCES [User](UserId)
);

CREATE TABLE ProjectMember(
    ProjectId INT NOT NULL,
    UserId BIGINT NOT NULL,
    Role NVARCHAR(100) NOT NULL,
    PRIMARY KEY (ProjectId, UserId),
    FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId),
    FOREIGN KEY (UserId) REFERENCES [User](UserId)
);

CREATE TABLE ProjectInvitation(
    InvitationId INT IDENTITY PRIMARY KEY,
    ProjectId INT NOT NULL,
    Email NVARCHAR(255) NOT NULL,
    Status NVARCHAR(50) NOT NULL DEFAULT 'Pending',
    SentAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId)
);

CREATE TABLE VisionScope(
    VisionScopeId INT IDENTITY PRIMARY KEY,
    ProjectId INT NOT NULL,
    [Content] TEXT,
    FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId)
);

CREATE TABLE [Constraint](
    ConstraintId INT IDENTITY PRIMARY KEY,
    ProjectId INT NOT NULL,
    [Type] NVARCHAR(100) NOT NULL,
    Description NVARCHAR(MAX),
    FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId) ON DELETE CASCADE
);

CREATE TABLE FunctionalRequirement (
    RequirementId INT IDENTITY PRIMARY KEY,
    ProjectId INT NOT NULL,
    Title NVARCHAR(255) NOT NULL,
    Description NVARCHAR(MAX),
    FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId)
);

CREATE TABLE NonFunctionalRequirement (
    RequirementId INT IDENTITY PRIMARY KEY,
    ProjectId INT NOT NULL,
    Category VARCHAR(50) NOT NULL CHECK (Category IN ('USABILITY', 'PERFORMANCE', 'SECURITY', 'SCALABILITY')),
    Description NVARCHAR(MAX),
    FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId) ON DELETE CASCADE
);

CREATE TABLE Usecase(
    UsecaseId INT IDENTITY PRIMARY KEY,
    ProjectId INT NOT NULL,
    CreatedBy BIGINT NOT NULL,
    FunctionalRequirementId INT NOT NULL,
    UsecaseName NVARCHAR(255) NOT NULL,
    Precondition NVARCHAR(MAX),
    Postcondition NVARCHAR(MAX),
    Exceptions NVARCHAR(MAX),
    [Priority] VARCHAR(50) CHECK (Priority IN ('HIGH', 'MEDIUM', 'LOW')),
    FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId) ON DELETE CASCADE,
    FOREIGN KEY (CreatedBy) REFERENCES [User](UserId) ON DELETE NO ACTION
);

CREATE TABLE UsecaseDiagramUrl(
    UsecaseId INT PRIMARY KEY,
    DiagramUrl TEXT,
    FOREIGN KEY (UsecaseId) REFERENCES Usecase(UsecaseId)
);

CREATE TABLE Actor(
    ActorId INT IDENTITY PRIMARY KEY,
    ProjectId INT NOT NULL,
    ActorName NVARCHAR(255) NOT NULL,
    Description NVARCHAR(MAX),
    FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId) ON DELETE CASCADE
);

CREATE TABLE UsecaseActor(
    UsecaseId INT NOT NULL,
    ActorId INT NOT NULL,
    PRIMARY KEY (UsecaseId, ActorId),
    FOREIGN KEY (UsecaseId) REFERENCES Usecase(UsecaseId) ON DELETE CASCADE,
    FOREIGN KEY (ActorId) REFERENCES Actor(ActorId) ON DELETE NO ACTION
);

CREATE TABLE UsecaseFlow(
    FlowId INT IDENTITY PRIMARY KEY,
    UsecaseId INT NOT NULL,
    FlowType VARCHAR(20) NOT NULL CHECK (FlowType IN ('NORMAL', 'ALTERNATIVE')),
    Description NVARCHAR(MAX),
    IsAlternative BIT NOT NULL DEFAULT 0,
    FOREIGN KEY (UsecaseId) REFERENCES Usecase(UsecaseId) ON DELETE CASCADE
);

CREATE TABLE BusinessRule(
    RuleId INT IDENTITY PRIMARY KEY,
    ProjectId INT NOT NULL,
    RuleDescription NVARCHAR(MAX),
    FOREIGN KEY (ProjectId) REFERENCES Project(ProjectId)
);

CREATE TABLE UsecaseBusinessRule(
    UsecaseId INT NOT NULL,
    RuleId INT NOT NULL,
    PRIMARY KEY (UsecaseId, RuleId),
    FOREIGN KEY (UsecaseId) REFERENCES Usecase(UsecaseId) ON DELETE CASCADE,
    FOREIGN KEY (RuleId) REFERENCES BusinessRule(RuleId) ON DELETE NO ACTION
);