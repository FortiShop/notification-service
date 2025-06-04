package org.fortishop.notificationservice.kafka;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fortishop.notificationservice.domain.NotificationTemplate;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.event.DeliveryCompletedEvent;
import org.fortishop.notificationservice.dto.event.DeliveryStartedEvent;
import org.fortishop.notificationservice.dto.event.PaymentCompletedEvent;
import org.fortishop.notificationservice.dto.event.PaymentFailedEvent;
import org.fortishop.notificationservice.dto.event.PointChangedEvent;
import org.fortishop.notificationservice.exception.NotificationException;
import org.fortishop.notificationservice.exception.NotificationExceptionType;
import org.fortishop.notificationservice.repository.NotificationTemplateRepository;
import org.fortishop.notificationservice.service.NotificationService;
import org.fortishop.notificationservice.service.NotificationSettingService;
import org.fortishop.notificationservice.sse.SseEmitterManager;
import org.fortishop.notificationservice.utils.NotificationOrderClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;
    private final NotificationSettingService settingService;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationOrderClient orderClient;
    private final SseEmitterManager sseEmitterManager;

    @KafkaListener(topics = "payment.completed", groupId = "notification-group", containerFactory = "paymentCompletedListenerContainerFactory")
    public void consumePaymentCompleted(PaymentCompletedEvent event, Acknowledgment ack) {
        try {
            Long memberId = orderClient.getMemberIdByOrderId(event.getOrderId());
            if (memberId == null || !settingService.isEnabled(memberId, NotificationType.ORDER)) return;

            Map<String, String> vars = Map.of(
                    "orderId", event.getOrderId().toString(),
                    "amount", event.getPaidAmount().toString()
            );
            String defaultMsg = "주문번호 " + event.getOrderId() + "에 대한 결제가 완료되었습니다. 금액: " + event.getPaidAmount() + "원";
            String message = generateTemplateMessage(NotificationType.ORDER, vars, defaultMsg);

            notificationService.createNotification(memberId, NotificationType.ORDER, message, event.getTraceId());
            sseEmitterManager.sendToUser(memberId, message);

            log.info("결제 성공 알림 전송 완료 - memberId={}, orderId={}, traceId={}", memberId, event.getOrderId(), event.getTraceId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("consumePaymentCompleted 처리 실패", e);
            throw e;
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-group", containerFactory = "paymentFailedListenerContainerFactory")
    public void consumePaymentFailed(PaymentFailedEvent event, Acknowledgment ack) {
        try {
            Long memberId = orderClient.getMemberIdByOrderId(event.getOrderId());
            if (memberId == null || !settingService.isEnabled(memberId, NotificationType.ORDER)) return;

            String message = "결제가 실패하였습니다: " + event.getReason();
            notificationService.createNotification(memberId, NotificationType.ORDER, message, event.getTraceId());
            sseEmitterManager.sendToUser(memberId, message);

            log.info("결제 실패 알림 전송 완료 - memberId={}, orderId={}, traceId={}", memberId, event.getOrderId(), event.getTraceId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("consumePaymentFailed 처리 실패", e);
            throw e;
        }
    }

    @KafkaListener(topics = "point.changed", groupId = "notification-group", containerFactory = "pointChangedListenerContainerFactory")
    public void consumePointChanged(PointChangedEvent event, Acknowledgment ack) {
        try {
            if (!settingService.isEnabled(event.getMemberId(), NotificationType.POINT)) return;

            String defaultMsg;
            switch (event.getChangeType()) {
                case "SAVE" -> defaultMsg = "포인트가 " + event.getAmount() + "원 적립되었습니다.";
                case "USE" -> defaultMsg = "포인트가 " + event.getAmount() + "원 사용되었습니다.";
                case "CANCEL" -> defaultMsg = "포인트가 " + event.getAmount() + "원 적립 취소되었습니다.";
                default -> defaultMsg = "포인트 변경 알림 - " + event.getAmount() + "원";
            }

            notificationService.createNotification(event.getMemberId(), NotificationType.POINT, defaultMsg, event.getTraceId());
            sseEmitterManager.sendToUser(event.getMemberId(), defaultMsg);

            log.info("포인트 알림 전송 완료 - memberId={}, type={}, amount={}", event.getMemberId(), event.getChangeType(), event.getAmount());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("consumePointChanged 처리 실패", e);
            throw e;
        }
    }

    @KafkaListener(topics = "delivery.started", groupId = "notification-group", containerFactory = "deliveryStartedListenerContainerFactory")
    public void consumeDeliveryStarted(DeliveryStartedEvent event, Acknowledgment ack) {
        try {
            Long memberId = orderClient.getMemberIdByOrderId(event.getOrderId());
            if (memberId == null || !settingService.isEnabled(memberId, NotificationType.DELIVERY)) return;

            Map<String, String> vars = Map.of(
                    "orderId", event.getOrderId().toString(),
                    "trackingNumber", event.getTrackingNumber()
            );
            String defaultMsg = "배송이 시작되었습니다. 운송장: " + event.getTrackingNumber();
            String message = generateTemplateMessage(NotificationType.DELIVERY, vars, defaultMsg);

            notificationService.createNotification(memberId, NotificationType.DELIVERY, message, event.getTraceId());
            sseEmitterManager.sendToUser(memberId, message);

            log.info("배송 시작 알림 전송 완료 - memberId={}, orderId={}, traceId={}", memberId, event.getOrderId(), event.getTraceId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("consumeDeliveryStarted 처리 실패", e);
            throw e;
        }
    }

    @KafkaListener(topics = "delivery.completed", groupId = "notification-group", containerFactory = "deliveryCompletedListenerContainerFactory")
    public void consumeDeliveryCompleted(DeliveryCompletedEvent event, Acknowledgment ack) {
        try {
            Long memberId = orderClient.getMemberIdByOrderId(event.getOrderId());
            if (memberId == null || !settingService.isEnabled(memberId, NotificationType.DELIVERY)) return;

            Map<String, String> vars = Map.of("orderId", event.getOrderId().toString());
            String defaultMsg = "배송이 완료되었습니다. 감사합니다.";
            String message = generateTemplateMessage(NotificationType.DELIVERY, vars, defaultMsg);

            notificationService.createNotification(memberId, NotificationType.DELIVERY, message, event.getTraceId());
            sseEmitterManager.sendToUser(memberId, message);

            log.info("배송 완료 알림 전송 완료 - memberId={}, orderId={}, traceId={}", memberId, event.getOrderId(), event.getTraceId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("consumeDeliveryCompleted 처리 실패", e);
            throw e;
        }
    }

    private String generateTemplateMessage(NotificationType type, Map<String, String> variables, String fallbackMessage) {
        return templateRepository.findByType(type)
                .map(template -> {
                    String msg = template.getMessage();
                    for (Map.Entry<String, String> entry : variables.entrySet()) {
                        msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
                    }
                    return msg;
                })
                .orElse(fallbackMessage);
    }

    @KafkaListener(topics = "payment.completed.dlq", groupId = "notification-dlq-group")
    public void handleDlq(PaymentCompletedEvent event) {
        log.error("[DLQ 메시지 확인] payment.completed 처리 실패 : {}", event);
        // slack 또는 이메일로 개발자, 관리자에게 알림
    }

    @KafkaListener(topics = "payment.failed.dlq", groupId = "notification-dlq-group")
    public void handleDlq(PaymentFailedEvent event) {
        log.error("[DLQ 메시지 확인] payment.failed 처리 실패 : {}", event);
        // slack 또는 이메일로 개발자, 관리자에게 알림
    }

    @KafkaListener(topics = "point.changed.dlq", groupId = "notification-dlq-group")
    public void handleDlq(PointChangedEvent event) {
        log.error("[DLQ 메시지 확인] point.changed 처리 실패 : {}", event);
        // slack 또는 이메일로 개발자, 관리자에게 알림
    }

    @KafkaListener(topics = "delivery.started.dlq", groupId = "notification-dlq-group")
    public void handleDlq(DeliveryStartedEvent event) {
        log.error("[DLQ 메시지 확인] delivery.started 처리 실패 : {}", event);
        // slack 또는 이메일로 개발자, 관리자에게 알림
    }

    @KafkaListener(topics = "delivery.completed.dlq", groupId = "notification-dlq-group")
    public void handleDlq(DeliveryCompletedEvent event) {
        log.error("[DLQ 메시지 확인] delivery.completed 처리 실패 : {}", event);
        // slack 또는 이메일로 개발자, 관리자에게 알림
    }
}
