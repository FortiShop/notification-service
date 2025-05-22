package org.fortishop.notificationservice.sse;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SseEmitterManager {

    private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    private static final long TIMEOUT = Duration.ofMinutes(30).toMillis();

    /**
     * SSE 연결 시 Emitter 생성 및 저장
     */
    public SseEmitter connect(Long memberId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitterMap.put(memberId, emitter);

        emitter.onCompletion(() -> {
            emitterMap.remove(memberId);
            log.info("SSE 연결 종료 - memberId={}", memberId);
        });

        emitter.onTimeout(() -> {
            emitterMap.remove(memberId);
            log.info("SSE 타임아웃 - memberId={}", memberId);
        });

        emitter.onError(e -> {
            emitterMap.remove(memberId);
            log.warn("SSE 에러 발생 - memberId={}, error={}", memberId, e.getMessage());
        });

        log.info("SSE 연결 생성 - memberId={}", memberId);
        return emitter;
    }

    /**
     * 사용자에게 알림 전송
     */
    public void sendToUser(Long memberId, Object data) {
        SseEmitter emitter = emitterMap.get(memberId);
        if (emitter == null) {
            log.info("SSE 연결 없음 - memberId={}", memberId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(data));
            log.info("SSE 알림 전송 완료 - memberId={}, data={}", memberId, data);
        } catch (IOException e) {
            emitterMap.remove(memberId);
            log.warn("SSE 전송 실패 - memberId={}, error={}", memberId, e.getMessage());
        }
    }
    
    public void complete(Long memberId) {
        SseEmitter emitter = emitterMap.remove(memberId);
        if (emitter != null) {
            emitter.complete();
        }
    }
}
