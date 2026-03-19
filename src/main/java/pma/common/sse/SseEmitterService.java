package pma.common.sse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 * Service quản lý vòng đời của SseEmitter cho từng user.
 * Dùng ConcurrentHashMap để thread-safe khi nhiều request đồng thời.
 */
@Service
@Slf4j
public class SseEmitterService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Đăng ký một SseEmitter mới cho user.
     */
    public SseEmitter addEmitter(Long userId) {
        // Timeout 30 phút — client sẽ reconnect tự động
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        emitter.onCompletion(() -> {
            log.debug("[SSE] Emitter completed for userId={}", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.debug("[SSE] Emitter timed out for userId={}", userId);
            emitters.remove(userId);
            emitter.complete();
        });
        emitter.onError(ex -> {
            log.debug("[SSE] Emitter error for userId={}: {}", userId, ex.getMessage());
            emitters.remove(userId);
        });

        emitters.put(userId, emitter);
        log.info("[SSE] Registered emitter for userId={}", userId);
        return emitter;
    }

    /**
     * Gửi một sự kiện SSE tới một user cụ thể.
     * Nếu user không còn subscribe thì bỏ qua (không throw exception).
     */
    public void sendEventToUser(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("[SSE] No emitter registered for userId={}, skipping event '{}'", userId, eventName);
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            log.info("[SSE] Sent event '{}' to userId={}", eventName, userId);
        } catch (IOException e) {
            log.warn("[SSE] Failed to send event '{}' to userId={}: {}", eventName, userId, e.getMessage());
            emitters.remove(userId);
            emitter.completeWithError(e);
        }
    }

    /**
     * Xóa emitter của user khỏi registry (dùng khi logout).
     */
    public void removeEmitter(Long userId) {
        SseEmitter removed = emitters.remove(userId);
        if (removed != null) {
            removed.complete();
            log.info("[SSE] Removed emitter for userId={}", userId);
        }
    }
}
