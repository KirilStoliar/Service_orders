package com.stoliar.auth.kafka;

import com.stoliar.events.user.UserCreated;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserCreatedProducer {

    public static final String TOPIC = "user.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserCreatedProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(
            String userId,
            String email,
            Instant createdAt
    ) {
        UserCreated event = UserCreated.newBuilder()
                .setUserId(userId)
                .setEmail(email)
                .setCreatedAt(createdAt)
                .build();

        kafkaTemplate.send(
                TOPIC,
                userId,
                event
        );
    }
}