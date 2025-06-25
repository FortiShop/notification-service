package org.fortishop.notificationservice.repository;

import org.fortishop.notificationservice.domain.NotificationSetting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingRepository extends MongoRepository<NotificationSetting, Long> {
}
