package com.marutixchange.notification_service.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for notification-service.
 *
 * Only active in production profile (--spring.profiles.active=prod).
 * In local/dev, Kafka is not required — service starts without it.
 *
 * - Concurrency = 5 (5 partitions per topic for 30L+ user scale)
 * - ExponentialBackOff retry: 1s → 2s → 4s → 8s (max 3 retries)
 * - ErrorHandlingDeserializer to prevent poison-pill messages
 */
@Configuration
@EnableKafka
@Profile("prod")   // Kafka only in production — dev/local starts without Kafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, NotificationEvent> consumerFactory() {
        JsonDeserializer<NotificationEvent> deserializer =
                new JsonDeserializer<>(NotificationEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("com.marutixchange.*");
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,          groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,   500);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent>
           kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(5);
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(8000L);
        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));

        return factory;
    }
}