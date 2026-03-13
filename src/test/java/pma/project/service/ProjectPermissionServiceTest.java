package pma.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pma.common.exception.CustomException.ForbiddenAccessException;
import pma.common.exception.CustomException.ProjectNotFoundException;
import pma.project.entity.member.Permission;
import pma.project.entity.member.ProjectMember;
import pma.project.entity.member.ProjectMemberId;
import pma.project.entity.member.ProjectRole;
import pma.project.repository.ProjectMemberRepository;

@ExtendWith(MockitoExtension.class)
class ProjectPermissionServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectPermissionService projectPermissionService;

    private ProjectMember mockMember;
    private ProjectRole mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new ProjectRole();
        mockRole.setName("EDITOR");

        mockMember = new ProjectMember();
        mockMember.setProjectRole(mockRole);
    }

    @Test
    @DisplayName("Should pass permission check if user has required permission")
    void testValidatePermission_Success() {
        // Arrange
        Permission perm = new Permission();
        perm.setCode("PROJECT_EDIT");
        mockRole.setPermissions(Set.of(perm));

        when(projectMemberRepository.findById(any(ProjectMemberId.class)))
                .thenReturn(Optional.of(mockMember));

        // Act & Assert
        assertDoesNotThrow(() -> 
            projectPermissionService.validatePermission(1L, 1, "PROJECT_EDIT")
        );
    }

    @Test
    @DisplayName("Should throw ForbiddenAccessException if user lacks required permission")
    void testValidatePermission_Forbidden() {
        // Arrange
        Permission perm = new Permission();
        perm.setCode("VIEW_ONLY_PERM");
        mockRole.setPermissions(Set.of(perm));

        when(projectMemberRepository.findById(any(ProjectMemberId.class)))
                .thenReturn(Optional.of(mockMember));

        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () -> 
            projectPermissionService.validatePermission(1L, 1, "PROJECT_EDIT")
        );
    }

    @Test
    @DisplayName("Should throw ProjectNotFoundException if user is not in project")
    void testValidatePermission_NotMember() {
        // Arrange
        when(projectMemberRepository.findById(any(ProjectMemberId.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProjectNotFoundException.class, () -> 
            projectPermissionService.validatePermission(1L, 1, "PROJECT_EDIT")
        );
    }

    @Test
    @DisplayName("Should return true if user has the specific role")
    void testHasRole_True() {
        // Arrange
        when(projectMemberRepository.findById(any(ProjectMemberId.class)))
                .thenReturn(Optional.of(mockMember));

        // Act & Assert
        assertTrue(projectPermissionService.hasRole(1L, 1, "EDITOR"));
    }

    @Test
    @DisplayName("Should return false if user does not have specific role")
    void testHasRole_False() {
        // Arrange
        when(projectMemberRepository.findById(any(ProjectMemberId.class)))
                .thenReturn(Optional.of(mockMember));

        // Act & Assert
        assertFalse(projectPermissionService.hasRole(1L, 1, "MAINTAINER"));
    }
}
