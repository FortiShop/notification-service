package org.fortishop.notificationservice.repository;

import java.util.List;
import org.fortishop.notificationservice.domain.Notification;
import org.fortishop.notificationservice.domain.NotificationStatus;
import org.fortishop.notificationservice.domain.NotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, Long> {

    // 최근 알림 20개 조회
    List<Notification> findTop20ByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 미읽음 알림 수
    Long countByMemberIdAndStatus(Long memberId, NotificationStatus status);

    // 읽음 처리용 (memberId 검증 포함)
    List<Notification> findByMemberIdAndIdIn(Long memberId, List<Long> ids);

    // 관리자 검색 (동적 쿼리는 QueryDSL 또는 @Query로 추후 확장 가능)
    List<Notification> findByMemberIdAndTypeAndStatus(Long memberId, NotificationType type, NotificationStatus status);
}
