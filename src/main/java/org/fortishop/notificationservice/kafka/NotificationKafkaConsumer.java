package org.fortishop.notificationservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.event.DeliveryCompletedEvent;
import org.fortishop.notificationservice.dto.event.DeliveryStartedEvent;
import org.fortishop.notificationservice.dto.event.PaymentCompletedEvent;
import org.fortishop.notificationservice.dto.event.PaymentFailedEvent;
import org.fortishop.notificationservice.dto.event.PointChangedEvent;
import org.fortishop.notificationservice.service.NotificationService;
import org.fortishop.notificationservice.service.NotificationSettingService;
import org.fortishop.notificationservice.sse.SseEmitterManager;
import org.fortishop.notificationservice.utils.NotificationOrderClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;
    private final NotificationSettingService settingService;
    private final NotificationOrderClient orderClient;
    private final SseEmitterManager sseEmitterManager;

    @KafkaListener(topics = "payment.completed", groupId = "notification-group", containerFactory = "paymentCompletedListenerContainerFactory")
    public void consumePaymentCompleted(PaymentCompletedEvent event) {
        Long memberId = orderClient.getMemberIdByOrderId(event.getOrderId());
        if (memberId == null || !settingService.isEnabled(memberId, NotificationType.ORDER)) {
            return;
        }

        String message = "결제가 완료되었습니다. 금액: " + event.getPaidAmount() + "원";
        notificationService.createNotification(memberId, NotificationType.ORDER, message);
        sseEmitterManager.sendToUser(memberId, message);
        log.info("결제 성공 알림 전송 완료 - memberId={}, orderId={}", memberId, event.getOrderId());
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-group", containerFactory = "paymentFailedListenerContainerFactory")
    public void consumePaymentFailed(PaymentFailedEvent event) {
        Long memberId = orderClient.getMemberIdByOrderId(event.getOrderId());
        if (memberId == null || !settingService.isEnabled(memberId, NotificationType.ORDER)) {
            return;
        }

        String message = "결제가 실패하였습니다: " + event.getReason();
        notificationService.createNotification(memberId, NotificationType.ORDER, message);
        sseEmitterManager.sendToUser(memberId, message);
        log.info("결제 실패 알림 전송 완료 - memberId={}, orderId={}", memberId, event.getOrderId());
    }

    @KafkaListener(topics = "point.changed", groupId = "notification-group", containerFactory = "pointChangedListenerContainerFactory")
    public void consumePointChanged(PointChangedEvent event) {
        log.info("포인트 알림 전송 확인 - memberId={}, type={}", event.getMemberId(), event.getChangeType());
        if (!settingService.isEnabled(event.getMemberId(), NotificationType.POINT)) {
            return;
        }

        String prefix = switch (event.getChangeType()) {
            case "SAVE" -> "포인트가 적립되었습니다.";
            case "USE" -> "포인트가 사용되었습니다.";
            case "CANCEL" -> "포인트가 복구되었습니다.";
            default -> "포인트 변경 알림";
        };

        String message = prefix + " 금액: " + event.getAmount() + "원";
        notificationService.createNotification(event.getMemberId(), NotificationType.POINT, message);
        sseEmitterManager.sendToUser(event.getMemberId(), message);
        log.info("포인트 알림 전송 완료 - memberId={}, type={}", event.getMemberId(), event.getChangeType());
    }

    @KafkaListener(topics = "delivery.started", groupId = "notification-group", containerFactory = "deliveryStartedListenerContainerFactory")
    public void consumeDeliveryStarted(DeliveryStartedEvent event) {
        Long memberId = orderClient.getMemberIdByOrderId(event.getOrderId());
        if (memberId == null || !settingService.isEnabled(memberId, NotificationType.DELIVERY)) {
            return;
        }

        String message = "배송이 시작되었습니다. 운송장: " + event.getTrackingNumber();
        notificationService.createNotification(memberId, NotificationType.DELIVERY, message);
        sseEmitterManager.sendToUser(memberId, message);
        log.info("배송 시작 알림 전송 완료 - memberId={}, orderId={}", memberId, event.getOrderId());
    }

    @KafkaListener(topics = "delivery.completed", groupId = "notification-group", containerFactory = "deliveryCompletedListenerContainerFactory")
    public void consumeDeliveryCompleted(DeliveryCompletedEvent event) {
        Long memberId = orderClient.getMemberIdByOrderId(event.getOrderId());
        if (memberId == null || !settingService.isEnabled(memberId, NotificationType.DELIVERY)) {
            return;
        }

        String message = "배송이 완료되었습니다. 감사합니다.";
        notificationService.createNotification(memberId, NotificationType.DELIVERY, message);
        sseEmitterManager.sendToUser(memberId, message);
        log.info("배송 완료 알림 전송 완료 - memberId={}, orderId={}", memberId, event.getOrderId());
    }
}
