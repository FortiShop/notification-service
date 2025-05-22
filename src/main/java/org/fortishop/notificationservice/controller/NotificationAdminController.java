package org.fortishop.notificationservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.fortishop.notificationservice.dto.request.NotificationTemplateRequest;
import org.fortishop.notificationservice.dto.response.NotificationResponse;
import org.fortishop.notificationservice.dto.response.NotificationTemplateResponse;
import org.fortishop.notificationservice.global.Responder;
import org.fortishop.notificationservice.service.AdminNotificationService;
import org.fortishop.notificationservice.service.NotificationTemplateService;
import org.fortishop.notificationservice.utils.AuthHeaderUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationAdminController {

    private final NotificationTemplateService templateService;
    private final AdminNotificationService adminService;

    /**
     * 템플릿 등록
     */
    @PostMapping("/templates")
    public ResponseEntity<?> createTemplate(HttpServletRequest httpServletRequest,
                                            @RequestBody NotificationTemplateRequest request) {
        AuthHeaderUtils.validateAdmin(httpServletRequest);
        templateService.create(request);
        return Responder.success("템플릿 등록 완료");
    }

    /**
     * 템플릿 수정
     */
    @PatchMapping("/templates/{id}")
    public ResponseEntity<?> updateTemplate(HttpServletRequest httpServletRequest,
                                            @PathVariable(name = "id") Long id,
                                            @RequestBody NotificationTemplateRequest request) {
        AuthHeaderUtils.validateAdmin(httpServletRequest);
        templateService.update(id, request);
        return Responder.success("템플릿 수정 완료");
    }

    /**
     * 템플릿 삭제
     */
    @DeleteMapping("/templates/{id}")
    public ResponseEntity<?> deleteTemplate(HttpServletRequest httpServletRequest, @PathVariable(name = "id") Long id) {
        AuthHeaderUtils.validateAdmin(httpServletRequest);
        templateService.delete(id);
        return Responder.success("템플릿 삭제 완료");
    }

    /**
     * 템플릿 전체 목록 조회
     */
    @GetMapping("/templates")
    public ResponseEntity<?> getAllTemplates(HttpServletRequest httpServletRequest) {
        AuthHeaderUtils.validateAdmin(httpServletRequest);
        List<NotificationTemplateResponse> templates = templateService.getAll();
        return Responder.success(templates);
    }

    /**
     * 알림 재전송
     */
    @PostMapping("/resend/{id}")
    public ResponseEntity<?> resend(HttpServletRequest httpServletRequest,
                                    @PathVariable(name = "id") Long id) {
        AuthHeaderUtils.validateAdmin(httpServletRequest);
        NotificationResponse result = adminService.resend(id);
        return Responder.success(result);
    }

    /**
     * 알림 조건 검색 (memberId, type, status)
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(HttpServletRequest httpServletRequest,
                                    @RequestParam(name = "memberId", required = false) Long memberId,
                                    @RequestParam(name = "type", required = false) String type,
                                    @RequestParam(name = "status", required = false) String status) {
        AuthHeaderUtils.validateAdmin(httpServletRequest);
        List<NotificationResponse> results = adminService.search(memberId, type, status);
        return Responder.success(results);
    }
}
