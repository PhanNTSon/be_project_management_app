package pma.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import pma.project.dto.request.RequestCreateInvitationDto;
import pma.project.dto.request.RequestUpdateMemberRoleDto;
import pma.project.dto.response.ResponseInvitationDto;
import pma.project.dto.response.ResponseMemberDto;
import pma.project.dto.response.ResponseProjectStatsDto;
import pma.project.service.ProjectMemberService;
import pma.user.entity.User;
import pma.user.repository.UserRepo;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final UserRepo userRepository;

    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getUserId();
    }

    @GetMapping("/stats")
    public ResponseEntity<ResponseProjectStatsDto> getProjectStats(
            @PathVariable Integer projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectMemberService.getProjectStats(projectId, userId));
    }

    @GetMapping("/members")
    public ResponseEntity<List<ResponseMemberDto>> getProjectMembers(
            @PathVariable Integer projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId, userId));
    }

    @GetMapping("/invitations")
    public ResponseEntity<List<ResponseInvitationDto>> getProjectInvitations(
            @PathVariable Integer projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectMemberService.getProjectInvitations(projectId, userId));
    }

    @PostMapping("/invitations")
    public ResponseEntity<ResponseInvitationDto> createInvitation(
            @PathVariable Integer projectId,
            @Valid @RequestBody RequestCreateInvitationDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectMemberService.createInvitation(projectId, userId, dto));
    }

    @DeleteMapping("/members/{targetUserId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Integer projectId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long authUserId = getCurrentUserId(userDetails);
        projectMemberService.removeMember(projectId, authUserId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/members/{targetUserId}/role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable Integer projectId,
            @PathVariable Long targetUserId,
            @Valid @RequestBody RequestUpdateMemberRoleDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long authUserId = getCurrentUserId(userDetails);
        projectMemberService.updateMemberRole(projectId, authUserId, targetUserId, dto);
        return ResponseEntity.ok().build();
    }
}
