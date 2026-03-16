package pma.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import pma.common.exception.ApiException;
import pma.project.dto.request.RequestCreateInvitationDto;
import pma.project.dto.request.RequestUpdateMemberRoleDto;
import pma.project.dto.response.ResponseInvitationDto;
import pma.project.dto.response.ResponseMemberDto;
import pma.project.entity.core.Project;
import pma.project.entity.member.ProjectInvitation;
import pma.project.entity.member.ProjectMember;
import pma.project.entity.member.ProjectRole;
import pma.project.repository.ProjectInvitationRepository;
import pma.project.repository.ProjectMemberRepository;
import pma.project.repository.ProjectRepository;
import pma.project.repository.ProjectRoleRepository;
import pma.user.dto.request.RequestRespondInvitationDto;
import pma.user.dto.response.ResponseUserInvitationDto;
import pma.user.entity.User;
import pma.user.repository.UserRepo;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private ProjectInvitationRepository projectInvitationRepository;
    @Mock private ProjectRoleRepository projectRoleRepository;
    @Mock private UserRepo userRepository;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    // ─── Shared fixtures ────────────────────────────────────────────────────
    private Project mockProject;
    private User mockOwnerUser;
    private User mockRegularUser;
    private ProjectRole ownerRole;
    private ProjectRole viewerRole;
    private ProjectMember ownerMember;
    private ProjectMember regularMember;

    @BeforeEach
    void setUp() throws Exception {
        // Project
        mockProject = new Project();
        java.lang.reflect.Field projId = Project.class.getDeclaredField("projectId");
        projId.setAccessible(true);
        projId.set(mockProject, 1);
        mockProject.setProjectName("Test Project");

        // Roles
        ownerRole = new ProjectRole("OWNER", "Project owner");
        viewerRole = new ProjectRole("VIEWER", "Read-only access");

        // Users — using the validated constructor
        mockOwnerUser = new User("owner@test.com", "owneruser", "password123");
        java.lang.reflect.Field ownerIdField = User.class.getDeclaredField("userId");
        ownerIdField.setAccessible(true);
        ownerIdField.set(mockOwnerUser, 1L);

        mockRegularUser = new User("user@test.com", "regularuser", "password123");
        java.lang.reflect.Field regularIdField = User.class.getDeclaredField("userId");
        regularIdField.setAccessible(true);
        regularIdField.set(mockRegularUser, 2L);

        // Members
        ownerMember = new ProjectMember();
        ownerMember.setProjectRole(ownerRole);

        regularMember = new ProjectMember();
        regularMember.setProjectRole(viewerRole);
    }

    // =====================================================================
    // getProjectStats
    // =====================================================================
    @Nested
    @DisplayName("getProjectStats")
    class GetProjectStatsTests {

        @Test
        @DisplayName("Should return stats for a valid project member")
        void shouldReturnStats() {
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 1L))
                    .thenReturn(Optional.of(ownerMember));
            when(projectMemberRepository.countByIdProjectId(1)).thenReturn(3);
            when(projectInvitationRepository.countByProject_ProjectIdAndStatus(1, "Pending")).thenReturn(2);

            var stats = projectMemberService.getProjectStats(1, 1L);

            assertEquals(3, stats.getTotalMembers());
            assertEquals(2, stats.getPendingInvites());
        }

        @Test
        @DisplayName("Should throw 403 when user is not a member")
        void shouldThrow403WhenNotMember() {
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 99L))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> projectMemberService.getProjectStats(1, 99L));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        }
    }

    // =====================================================================
    // getProjectMembers
    // =====================================================================
    @Nested
    @DisplayName("getProjectMembers")
    class GetProjectMembersTests {

        @Test
        @DisplayName("Should return member DTOs")
        void shouldReturnMemberDtos() {
            ownerMember = new ProjectMember(mockProject, mockOwnerUser, ownerRole);

            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 1L))
                    .thenReturn(Optional.of(ownerMember));
            when(projectMemberRepository.findAllByProjectIdWithUserAndRole(1))
                    .thenReturn(List.of(ownerMember));

            List<ResponseMemberDto> members = projectMemberService.getProjectMembers(1, 1L);

            assertEquals(1, members.size());
            assertEquals("owner@test.com", members.get(0).getEmail());
            assertEquals("OWNER", members.get(0).getRoleName());
        }
    }

    // =====================================================================
    // createInvitation
    // =====================================================================
    @Nested
    @DisplayName("createInvitation")
    class CreateInvitationTests {

        @Test
        @DisplayName("OWNER can send a new invitation")
        void ownerCanSendInvitation() {
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 1L))
                    .thenReturn(Optional.of(ownerMember));
            when(projectRepository.findById(1)).thenReturn(Optional.of(mockProject));
            when(projectInvitationRepository.existsByProject_ProjectIdAndEmailAndStatus(1, "user@test.com", "Pending"))
                    .thenReturn(false);

            ProjectInvitation saved = new ProjectInvitation();
            saved.setEmail("user@test.com");
            saved.setStatus("Pending");
            saved.setSentAt(LocalDateTime.now());
            when(projectInvitationRepository.save(any(ProjectInvitation.class))).thenReturn(saved);

            RequestCreateInvitationDto dto = new RequestCreateInvitationDto("user@test.com");
            ResponseInvitationDto result = projectMemberService.createInvitation(1, 1L, dto);

            assertEquals("user@test.com", result.getEmail());
            assertEquals("Pending", result.getStatus());
        }

        @Test
        @DisplayName("Should throw 403 when non-owner tries to create invitation")
        void nonOwnerCannotSendInvitation() {
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 2L))
                    .thenReturn(Optional.of(regularMember));

            RequestCreateInvitationDto dto = new RequestCreateInvitationDto("other@test.com");
            ApiException ex = assertThrows(ApiException.class,
                    () -> projectMemberService.createInvitation(1, 2L, dto));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        }

        @Test
        @DisplayName("Should throw 400 when a pending invitation already exists")
        void shouldThrowWhenDuplicatePendingInvitation() {
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 1L))
                    .thenReturn(Optional.of(ownerMember));
            when(projectRepository.findById(1)).thenReturn(Optional.of(mockProject));
            when(projectInvitationRepository.existsByProject_ProjectIdAndEmailAndStatus(1, "user@test.com", "Pending"))
                    .thenReturn(true);

            RequestCreateInvitationDto dto = new RequestCreateInvitationDto("user@test.com");
            ApiException ex = assertThrows(ApiException.class,
                    () -> projectMemberService.createInvitation(1, 1L, dto));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }
    }

    // =====================================================================
    // removeMember
    // =====================================================================
    @Nested
    @DisplayName("removeMember")
    class RemoveMemberTests {

        @Test
        @DisplayName("OWNER can remove another member")
        void ownerCanRemoveMember() {
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 1L))
                    .thenReturn(Optional.of(ownerMember));
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 2L))
                    .thenReturn(Optional.of(regularMember));

            assertDoesNotThrow(() -> projectMemberService.removeMember(1, 1L, 2L));
            verify(projectMemberRepository, times(1)).delete(regularMember);
        }

        @Test
        @DisplayName("Should throw 400 when owner tries to remove themselves")
        void ownerCannotRemoveThemselves() {
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 1L))
                    .thenReturn(Optional.of(ownerMember));

            ApiException ex = assertThrows(ApiException.class,
                    () -> projectMemberService.removeMember(1, 1L, 1L));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }

        @Test
        @DisplayName("Should throw 403 when non-owner tries to remove member")
        void nonOwnerCannotRemoveMember() {
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 2L))
                    .thenReturn(Optional.of(regularMember));

            ApiException ex = assertThrows(ApiException.class,
                    () -> projectMemberService.removeMember(1, 2L, 1L));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        }
    }

    // =====================================================================
    // updateMemberRole
    // =====================================================================
    @Nested
    @DisplayName("updateMemberRole")
    class UpdateMemberRoleTests {

        @Test
        @DisplayName("OWNER can update another member's role")
        void ownerCanUpdateRole() {
            ProjectRole editorRole = new ProjectRole("EDITOR", "Can edit");
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 1L))
                    .thenReturn(Optional.of(ownerMember));
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 2L))
                    .thenReturn(Optional.of(regularMember));
            when(projectRoleRepository.findByName("EDITOR")).thenReturn(Optional.of(editorRole));

            RequestUpdateMemberRoleDto dto = new RequestUpdateMemberRoleDto("EDITOR");
            assertDoesNotThrow(() -> projectMemberService.updateMemberRole(1, 1L, 2L, dto));

            verify(projectMemberRepository, times(1)).save(regularMember);
            assertEquals("EDITOR", regularMember.getProjectRole().getName());
        }

        @Test
        @DisplayName("Should throw 400 when owner tries to update own role")
        void ownerCannotUpdateOwnRole() {
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 1L))
                    .thenReturn(Optional.of(ownerMember));

            RequestUpdateMemberRoleDto dto = new RequestUpdateMemberRoleDto("VIEWER");
            ApiException ex = assertThrows(ApiException.class,
                    () -> projectMemberService.updateMemberRole(1, 1L, 1L, dto));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }

        @Test
        @DisplayName("Should throw 400 for invalid role name")
        void shouldThrowForInvalidRole() {
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 1L))
                    .thenReturn(Optional.of(ownerMember));
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 2L))
                    .thenReturn(Optional.of(regularMember));
            when(projectRoleRepository.findByName("SUPERADMIN")).thenReturn(Optional.empty());

            RequestUpdateMemberRoleDto dto = new RequestUpdateMemberRoleDto("SUPERADMIN");
            ApiException ex = assertThrows(ApiException.class,
                    () -> projectMemberService.updateMemberRole(1, 1L, 2L, dto));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }
    }

    // =====================================================================
    // respondToInvitation
    // =====================================================================
    @Nested
    @DisplayName("respondToInvitation")
    class RespondToInvitationTests {

        private ProjectInvitation pendingInvitation;

        @BeforeEach
        void setUpInvitation() {
            pendingInvitation = new ProjectInvitation();
            pendingInvitation.setEmail("user@test.com");
            pendingInvitation.setStatus("Pending");
            pendingInvitation.setProject(mockProject);
        }

        @Test
        @DisplayName("User can accept a pending invitation and becomes a member")
        void userCanAcceptInvitation() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(mockRegularUser));
            when(projectInvitationRepository.findById(10)).thenReturn(Optional.of(pendingInvitation));
            when(projectRoleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewerRole));
            when(projectMemberRepository.findByProject_ProjectIdAndUser_UserId(1, 2L))
                    .thenReturn(Optional.empty());

            RequestRespondInvitationDto dto = new RequestRespondInvitationDto(true);
            assertDoesNotThrow(() -> projectMemberService.respondToInvitation(2L, 10, dto));

            assertEquals("Accepted", pendingInvitation.getStatus());
            verify(projectInvitationRepository, times(1)).save(pendingInvitation);
            verify(projectRepository, times(1)).save(mockProject);
        }

        @Test
        @DisplayName("User can reject a pending invitation")
        void userCanRejectInvitation() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(mockRegularUser));
            when(projectInvitationRepository.findById(10)).thenReturn(Optional.of(pendingInvitation));

            RequestRespondInvitationDto dto = new RequestRespondInvitationDto(false);
            assertDoesNotThrow(() -> projectMemberService.respondToInvitation(2L, 10, dto));

            assertEquals("Rejected", pendingInvitation.getStatus());
            verify(projectInvitationRepository, times(1)).save(pendingInvitation);
        }

        @Test
        @DisplayName("Should throw 403 when invitation is not for the requesting user")
        void shouldThrowWhenInvitationDoesNotBelongToUser() {
            User otherUser = new User("other@test.com", "otheruser", "password123");

            when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
            when(projectInvitationRepository.findById(10)).thenReturn(Optional.of(pendingInvitation));

            ApiException ex = assertThrows(ApiException.class,
                    () -> projectMemberService.respondToInvitation(2L, 10, new RequestRespondInvitationDto(true)));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        }

        @Test
        @DisplayName("Should throw 400 when invitation is not in Pending state")
        void shouldThrowWhenInvitationAlreadyProcessed() {
            pendingInvitation.setStatus("Accepted");

            when(userRepository.findById(2L)).thenReturn(Optional.of(mockRegularUser));
            when(projectInvitationRepository.findById(10)).thenReturn(Optional.of(pendingInvitation));

            ApiException ex = assertThrows(ApiException.class,
                    () -> projectMemberService.respondToInvitation(2L, 10, new RequestRespondInvitationDto(true)));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }
    }

    // =====================================================================
    // getUserInvitations
    // =====================================================================
    @Nested
    @DisplayName("getUserInvitations")
    class GetUserInvitationsTests {

        @Test
        @DisplayName("Should return invitation DTOs for a user")
        void shouldReturnInvitationsForUser() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(mockRegularUser));

            ProjectInvitation inv = new ProjectInvitation();
            inv.setEmail("user@test.com");
            inv.setStatus("Pending");
            inv.setProject(mockProject);
            inv.setSentAt(LocalDateTime.now());

            when(projectInvitationRepository.findByEmail("user@test.com")).thenReturn(List.of(inv));

            List<ResponseUserInvitationDto> result = projectMemberService.getUserInvitations(2L);

            assertEquals(1, result.size());
            assertEquals("Test Project", result.get(0).getProjectName());
            assertEquals("Pending", result.get(0).getStatus());
        }

        @Test
        @DisplayName("Should throw 404 when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> projectMemberService.getUserInvitations(99L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }
    }
}
