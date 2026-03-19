package pma.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pma.common.exception.ApiException;
import pma.common.sse.SseEmitterService;
import pma.project.dto.request.RequestCreateInvitationDto;
import pma.project.dto.response.ResponseInvitationDto;
import pma.project.dto.response.ResponseMemberDto;
import pma.project.dto.response.ResponseProjectStatsDto;
import pma.project.entity.core.Project;
import pma.project.entity.member.ProjectInvitation;
import pma.project.entity.member.ProjectMember;
import pma.project.entity.member.ProjectRole;
import pma.project.repository.ProjectInvitationRepository;
import pma.project.repository.ProjectMemberRepository;
import pma.project.repository.ProjectRepository;
import pma.project.repository.ProjectRoleRepository;
import pma.project.dto.request.RequestUpdateMemberRoleDto;
import pma.user.dto.request.RequestRespondInvitationDto;
import pma.user.dto.response.ResponseUserInvitationDto;
import pma.user.entity.User;
import pma.user.repository.UserRepo;

import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectInvitationRepository projectInvitationRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final UserRepo userRepository;
    private final SseEmitterService sseEmitterService;

    private ProjectMember getMemberOrThrow(Integer projectId, Long userId) {
        return projectMemberRepository.findByProject_ProjectIdAndUser_UserId(projectId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this project"));
    }

    private void checkIsOwner(ProjectMember authMember) {
        if (!"OWNER".equals(authMember.getProjectRole().getName())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Owner permission required");
        }
    }

    @Transactional(readOnly = true)
    public ResponseProjectStatsDto getProjectStats(Integer projectId, Long authUserId) {
        getMemberOrThrow(projectId, authUserId);
        Integer totalSubs = projectMemberRepository.countByIdProjectId(projectId);
        Integer pendingInvites = projectInvitationRepository.countByProject_ProjectIdAndStatus(projectId, "Pending");
        return new ResponseProjectStatsDto(totalSubs != null ? totalSubs : 0, pendingInvites != null ? pendingInvites : 0);
    }

    @Transactional(readOnly = true)
    public List<ResponseMemberDto> getProjectMembers(Integer projectId, Long authUserId) {
        getMemberOrThrow(projectId, authUserId);
        List<ProjectMember> members = projectMemberRepository.findAllByProjectIdWithUserAndRole(projectId);
        return members.stream()
                .map(m -> ResponseMemberDto.builder()
                        .userId(m.getUser().getUserId())
                        .username(m.getUser().getUsername())
                        .email(m.getUser().getEmail())
                        .fullName(m.getUser().getFullName())
                        .roleName(m.getProjectRole().getName())
                        .joinedAt(null) // Can add a joined_at field to ProjectMember if needed
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResponseInvitationDto> getProjectInvitations(Integer projectId, Long authUserId) {
        ProjectMember authMember = getMemberOrThrow(projectId, authUserId);
        checkIsOwner(authMember);

        List<ProjectInvitation> invites = projectInvitationRepository.findByProject_ProjectId(projectId);
        return invites.stream()
                .map(i -> ResponseInvitationDto.builder()
                        .invitationId(i.getInvitationId())
                        .email(i.getEmail())
                        .status(i.getStatus())
                        .sentAt(i.getSentAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ResponseInvitationDto createInvitation(Integer projectId, Long authUserId, RequestCreateInvitationDto dto) {
        ProjectMember authMember = getMemberOrThrow(projectId, authUserId);
        checkIsOwner(authMember);

        // Lấy project qua navigation thay vì gọi thêm 1 DB query nữa
        Project project = authMember.getProject();

        boolean exists = projectInvitationRepository.existsByProject_ProjectIdAndEmailAndStatus(projectId, dto.getEmail(), "Pending");
        if (exists) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A pending invitation already exists for this email");
        }

        ProjectInvitation inv = new ProjectInvitation();
        inv.setProject(project);
        inv.setEmail(dto.getEmail());
        inv.setStatus("Pending");
        inv.setSentAt(java.time.LocalDateTime.now());
        final ProjectInvitation savedInv = projectInvitationRepository.save(inv);

        // Gửi SSE event cho user có email này nếu họ đang online
        userRepository.findByEmail(dto.getEmail()).ifPresent(targetUser ->
                sseEmitterService.sendEventToUser(
                        targetUser.getUserId(),
                        "INVITATION_RECEIVED",
                        java.util.Map.of(
                                "invitationId", savedInv.getInvitationId(),
                                "projectId",    projectId,
                                "projectName",  project.getProjectName()
                        )
                ));

        return ResponseInvitationDto.builder()
                .invitationId(savedInv.getInvitationId())
                .email(savedInv.getEmail())
                .status(savedInv.getStatus())
                .sentAt(savedInv.getSentAt())
                .build();
    }

    @Transactional
    public void removeMember(Integer projectId, Long authUserId, Long targetUserId) {
        ProjectMember authMember = getMemberOrThrow(projectId, authUserId);
        checkIsOwner(authMember);

        // Cannot remove yourself (the owner must transfer ownership first or delete project)
        if (authUserId.equals(targetUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot remove yourself from the project. Transfer ownership or delete the project instead.");
        }

        ProjectMember targetMember = projectMemberRepository.findByProject_ProjectIdAndUser_UserId(projectId, targetUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target member not found in this project"));

        projectMemberRepository.delete(targetMember);
    }

    @Transactional
    public void updateMemberRole(Integer projectId, Long authUserId, Long targetUserId, RequestUpdateMemberRoleDto dto) {
        ProjectMember authMember = getMemberOrThrow(projectId, authUserId);
        checkIsOwner(authMember);

        if (authUserId.equals(targetUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot update your own role directly.");
        }

        ProjectMember targetMember = projectMemberRepository.findByProject_ProjectIdAndUser_UserId(projectId, targetUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target member not found in this project"));

        ProjectRole newRole = projectRoleRepository.findByName(dto.getRoleName())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid role name: " + dto.getRoleName()));

        targetMember.setProjectRole(newRole);
        projectMemberRepository.save(targetMember);

        // Gửi SSE event cho user bị thay đổi role
        sseEmitterService.sendEventToUser(
                targetUserId,
                "ROLE_UPDATED",
                java.util.Map.of(
                        "projectId", projectId,
                        "newRole",   dto.getRoleName()
                )
        );
    }

    // =====================================================================
    // USER INVITATIONS (RESPONDING)
    // =====================================================================

    @Transactional(readOnly = true)
    public List<ResponseUserInvitationDto> getUserInvitations(Long authUserId) {
        User user = userRepository.findById(authUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<ProjectInvitation> invites = projectInvitationRepository.findByEmail(user.getEmail());
        
        return invites.stream()
                .map(i -> ResponseUserInvitationDto.builder()
                        .invitationId(i.getInvitationId())
                        .projectId(i.getProject().getProjectId())
                        .projectName(i.getProject().getProjectName())
                        .status(i.getStatus())
                        .sentAt(i.getSentAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void respondToInvitation(Long authUserId, Integer invitationId, RequestRespondInvitationDto dto) {
        User user = userRepository.findById(authUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        ProjectInvitation invitation = projectInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invitation not found"));

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This invitation does not belong to you");
        }

        if (!"Pending".equalsIgnoreCase(invitation.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invitation has already been " + invitation.getStatus() + ".");
        }

        if (Boolean.TRUE.equals(dto.getAccept())) {
            invitation.setStatus("Accepted");
            
            // Generate ProjectMember with default role (e.g. Viewer or Editor)
            // Or maybe default to the lowest role if implicit, here we'll assume Viewer as a safe default.
            ProjectRole defaultRole = projectRoleRepository.findByName("VIEWER")
                    .orElseGet(() -> projectRoleRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No project roles found in system")));
            
            Project project = invitation.getProject();
            
            // Check if already a member
            boolean alreadyMember = projectMemberRepository.findByProject_ProjectIdAndUser_UserId(project.getProjectId(), user.getUserId()).isPresent();
            if (!alreadyMember) {
                ProjectMember newMember = new ProjectMember(project, user, defaultRole);
                project.getMembers().add(newMember);
                projectRepository.save(project);
            }
        } else {
            invitation.setStatus("Rejected");
        }

        projectInvitationRepository.save(invitation);
    }
}
