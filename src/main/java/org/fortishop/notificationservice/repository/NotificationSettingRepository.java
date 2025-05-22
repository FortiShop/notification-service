package org.fortishop.notificationservice.repository;

import org.fortishop.notificationservice.domain.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
}
