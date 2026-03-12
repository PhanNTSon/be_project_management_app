INSERT INTO [Permission]([Code], [Description]) VALUES
('VIEW_PROJECT','View project data'),
('EDIT_WITH_REQUEST','Edit data but requires change request'),
('EDIT_DIRECT','Edit project data directly'),
('MANAGE_MEMBER','Add/remove project members'),
('APPROVE_CHANGE','Approve change requests');

INSERT INTO ProjectRole([Name], [Description]) VALUES
('VIEWER','Can only view project data'),
('EDITOR','Can edit but must create change request'),
('MAINTAINER','Can edit directly'),
('OWNER','Full project control');

-- VIEWER
INSERT INTO ProjectRolePermission
SELECT pr.ProjectRoleId, p.PermissionId
FROM ProjectRole pr, [Permission] p
WHERE pr.Name='VIEWER' AND p.Code='VIEW_PROJECT';


-- EDITOR
INSERT INTO ProjectRolePermission
SELECT pr.ProjectRoleId, p.PermissionId
FROM ProjectRole pr, [Permission] p
WHERE pr.Name='EDITOR'
AND p.Code IN ('VIEW_PROJECT','EDIT_WITH_REQUEST');


-- MAINTAINER
INSERT INTO ProjectRolePermission
SELECT pr.ProjectRoleId, p.PermissionId
FROM ProjectRole pr, [Permission] p
WHERE pr.Name='MAINTAINER'
AND p.Code IN ('VIEW_PROJECT','EDIT_DIRECT');


-- OWNER
INSERT INTO ProjectRolePermission
SELECT pr.ProjectRoleId, p.PermissionId
FROM ProjectRole pr, [Permission] p
WHERE pr.Name='OWNER'
AND p.Code IN ('VIEW_PROJECT','EDIT_DIRECT','MANAGE_MEMBER','APPROVE_CHANGE');