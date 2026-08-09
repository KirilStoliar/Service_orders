package com.stoliar.auth.controller;

import com.stoliar.auth.kafka.UserCreatedProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/kafka")
public class KafkaTestController {

    private final UserCreatedProducer userCreatedProducer;

    public KafkaTestController(UserCreatedProducer userCreatedProducer) {
        this.userCreatedProducer = userCreatedProducer;
    }

    @PostMapping("/user-created")
    public ResponseEntity<Map<String, String>> publishUserCreated(
            @RequestParam String email
    ) {
        String userId = UUID.randomUUID().toString();

        userCreatedProducer.publish(
                userId,
                email,
                Instant.now()
        );

        return ResponseEntity.accepted().body(
                Map.of(
                        "userId", userId,
                        "email", email,
                        "status", "published"
                )
        );
    }
}