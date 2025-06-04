package org.fortishop.notificationservice.kafka.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.fortishop.notificationservice.dto.event.DeliveryCompletedEvent;
import org.fortishop.notificationservice.dto.event.DeliveryStartedEvent;
import org.fortishop.notificationservice.dto.event.PaymentCompletedEvent;
import org.fortishop.notificationservice.dto.event.PaymentFailedEvent;
import org.fortishop.notificationservice.dto.event.PointChangedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> paymentCompletedListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        return createListenerContainerFactory(PaymentCompletedEvent.class, "payment.completed.dlq", kafkaTemplate);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> paymentFailedListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        return createListenerContainerFactory(PaymentFailedEvent.class, "payment.failed.dlq", kafkaTemplate);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PointChangedEvent> pointChangedListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        return createListenerContainerFactory(PointChangedEvent.class, "point.changed.dlq", kafkaTemplate);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryStartedEvent> deliveryStartedListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        return createListenerContainerFactory(DeliveryStartedEvent.class, "delivery.started.dlq", kafkaTemplate);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryCompletedEvent> deliveryCompletedListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        return createListenerContainerFactory(DeliveryCompletedEvent.class, "delivery.completed.dlq", kafkaTemplate);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createListenerContainerFactory(Class<T> valueType, String dlqTopic, KafkaTemplate<String, Object> kafkaTemplate) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        factory.setConsumerFactory(createConsumerFactory(valueType));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(dlqTopic, record.partition())
        );
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3)));

        return factory;
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> valueType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        JsonDeserializer<T> deserializer = new JsonDeserializer<>(valueType);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(false);
        deserializer.setUseTypeMapperForKey(true);
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }
}
