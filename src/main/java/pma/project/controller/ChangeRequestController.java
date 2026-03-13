package pma.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import pma.project.dto.request.ChangeRequestSubmitDto;
import pma.project.entity.change.ChangeRequest;
import pma.project.service.ChangeRequestService;
import pma.user.entity.User;
import pma.user.repository.UserRepo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestController
@RequestMapping("/api/projects/{projectId}/change-requests")
@RequiredArgsConstructor
public class ChangeRequestController {

    private final ChangeRequestService changeRequestService;
    private final UserRepo userRepository;

    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getUserId();
    }

    @PostMapping
    public ResponseEntity<ChangeRequest> createChangeRequest(
            @PathVariable Integer projectId,
            @RequestBody ChangeRequestSubmitDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getCurrentUserId(userDetails);
        ChangeRequest createdRequest = changeRequestService.createChangeRequest(userId, projectId, dto);
        return ResponseEntity.ok(createdRequest);
    }

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<ChangeRequest> approveChangeRequest(
            @PathVariable Integer projectId,
            @PathVariable Integer requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getCurrentUserId(userDetails);
        // Note: projectId is mapped in Route to maintain RESTful structure
        ChangeRequest approvedRequest = changeRequestService.applyChangeRequest(userId, requestId);
        return ResponseEntity.ok(approvedRequest);
    }
}
