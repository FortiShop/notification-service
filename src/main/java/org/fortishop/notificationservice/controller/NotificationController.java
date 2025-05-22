package org.fortishop.notificationservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.fortishop.notificationservice.dto.request.NotificationReadRequest;
import org.fortishop.notificationservice.dto.request.NotificationSettingRequest;
import org.fortishop.notificationservice.dto.response.NotificationResponse;
import org.fortishop.notificationservice.dto.response.NotificationSettingResponse;
import org.fortishop.notificationservice.global.Responder;
import org.fortishop.notificationservice.service.NotificationService;
import org.fortishop.notificationservice.service.NotificationSettingService;
import org.fortishop.notificationservice.utils.AuthHeaderUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSettingService settingService;

    /**
     * 최근 알림 20개 조회
     */
    @GetMapping
    public ResponseEntity<?> getMyNotifications(HttpServletRequest request) {
        Long memberId = AuthHeaderUtils.extractMemberId(request);
        List<NotificationResponse> notifications = notificationService.getRecent(memberId);
        return Responder.success(notifications);
    }

    /**
     * 미읽음 알림 수 조회
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest request) {
        Long memberId = AuthHeaderUtils.extractMemberId(request);
        Long count = notificationService.getUnreadCount(memberId);
        return Responder.success(count);
    }

    /**
     * 알림 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationById(HttpServletRequest request,
                                                 @PathVariable(name = "id") Long id) {
        Long memberId = AuthHeaderUtils.extractMemberId(request);
        NotificationResponse response = notificationService.getById(memberId, id);
        return Responder.success(response);
    }

    /**
     * 알림 읽음 처리
     */
    @PostMapping("/read")
    public ResponseEntity<?> markAsRead(HttpServletRequest request,
                                        @RequestBody NotificationReadRequest dto) {
        Long memberId = AuthHeaderUtils.extractMemberId(request);
        notificationService.markAsRead(memberId, dto.getIds());
        return Responder.success("읽음 처리 완료");
    }

    /**
     * 알림 단건 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(HttpServletRequest request,
                                                @PathVariable(name = "id") Long id) {
        Long memberId = AuthHeaderUtils.extractMemberId(request);
        notificationService.delete(memberId, id);
        return Responder.success("삭제 완료");
    }

    /**
     * 알림 수신 설정 조회
     */
    @GetMapping("/settings")
    public ResponseEntity<?> getNotificationSettings(HttpServletRequest request) {
        Long memberId = AuthHeaderUtils.extractMemberId(request);
        NotificationSettingResponse settings = settingService.get(memberId);
        return Responder.success(settings);
    }

    /**
     * 알림 수신 설정 변경
     */
    @PatchMapping("/settings")
    public ResponseEntity<?> updateNotificationSettings(HttpServletRequest request,
                                                        @RequestBody NotificationSettingRequest dto) {
        Long memberId = AuthHeaderUtils.extractMemberId(request);
        settingService.update(memberId, dto);
        return Responder.success("알림 수신 설정 변경 완료");
    }
}
