package org.fortishop.notificationservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.fortishop.notificationservice.sse.SseEmitterManager;
import org.fortishop.notificationservice.utils.AuthHeaderUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationSseController {

    private final SseEmitterManager sseEmitterManager;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(HttpServletRequest request) {
        Long memberId = AuthHeaderUtils.extractMemberId(request);
        return sseEmitterManager.connect(memberId);
    }
}
