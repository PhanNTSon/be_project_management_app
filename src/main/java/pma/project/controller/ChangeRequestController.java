package pma.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import pma.project.dto.request.ChangeRequestSubmitDto;
import pma.project.dto.response.ResponseChangeRequestDto;
import pma.project.entity.change.ChangeRequest;
import pma.project.service.ChangeRequestService;
import pma.user.entity.User;
import pma.user.repository.UserRepo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

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

    /**
     * Lấy danh sách ChangeRequest của Project (chỉ MAINTAINER/OWNER).
     */
    @GetMapping
    public ResponseEntity<List<ResponseChangeRequestDto>> getChangeRequests(
            @PathVariable Integer projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(changeRequestService.getChangeRequestsByProject(userId, projectId));
    }

    /**
     * Tạo mới ChangeRequest (EDITOR: PENDING / MAINTAINER+OWNER: tự APPROVED).
     */
    @PostMapping
    public ResponseEntity<ChangeRequest> createChangeRequest(
            @PathVariable Integer projectId,
            @RequestBody ChangeRequestSubmitDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(changeRequestService.createChangeRequest(userId, projectId, dto));
    }

    /**
     * Phê duyệt ChangeRequest – áp dụng các thay đổi vào entity gốc.
     */
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ChangeRequest> approveChangeRequest(
            @PathVariable Integer projectId,
            @PathVariable Integer requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(changeRequestService.applyChangeRequest(userId, requestId));
    }

    /**
     * Từ chối ChangeRequest.
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ChangeRequest> rejectChangeRequest(
            @PathVariable Integer projectId,
            @PathVariable Integer requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(changeRequestService.rejectChangeRequest(userId, requestId));
    }
}
