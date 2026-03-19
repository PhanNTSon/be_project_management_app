package pma.common.sse;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pma.user.entity.User;
import pma.user.repository.UserRepo;

/**
 * Controller cung cấp endpoint SSE để client subscribe nhận sự kiện server-push.
 * Frontend kết nối một lần sau khi đăng nhập và giữ kết nối mở.
 */
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class SseController {

    private final SseEmitterService sseEmitterService;
    private final UserRepo userRepository;

    /**
     * Client gọi GET /api/sse/subscribe để bắt đầu nhận SSE events.
     * Trả về một SseEmitter stream (text/event-stream).
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        log.info("[SSE] User {} subscribed to SSE stream", user.getUserId());
        return sseEmitterService.addEmitter(user.getUserId());
    }
}
