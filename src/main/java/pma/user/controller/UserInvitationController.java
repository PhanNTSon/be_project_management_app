package pma.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import pma.project.service.ProjectMemberService;
import pma.user.dto.request.RequestRespondInvitationDto;
import pma.user.dto.response.ResponseUserInvitationDto;
import pma.user.entity.User;
import pma.user.repository.UserRepo;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/invitations")
@RequiredArgsConstructor
public class UserInvitationController {

    private final ProjectMemberService projectMemberService;
    private final UserRepo userRepository;

    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getUserId();
    }

    @GetMapping
    public ResponseEntity<List<ResponseUserInvitationDto>> getMyInvitations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectMemberService.getUserInvitations(userId));
    }

    @PostMapping("/{invitationId}/respond")
    public ResponseEntity<Void> respondToInvitation(
            @PathVariable Integer invitationId,
            @Valid @RequestBody RequestRespondInvitationDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        projectMemberService.respondToInvitation(userId, invitationId, dto);
        return ResponseEntity.ok().build();
    }
}
