package com.stoliar.auth.service;

import com.stoliar.auth.entity.AuthUser;
import com.stoliar.auth.entity.EmailVerificationToken;
import com.stoliar.auth.exception.InvalidCredentialsException;
import com.stoliar.auth.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository repository;
    private final Duration tokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            EmailVerificationTokenRepository repository,
            @Value("${security.jwt.email-verification-ttl:24h}")
            Duration tokenTtl
    ) {
        this.repository = repository;
        this.tokenTtl = tokenTtl;
    }

    @Transactional
    public String createToken(AuthUser user) {

        repository.deleteByUserId(user.getId());

        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);

        // Используем URL-safe Base64 без padding
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        // Сохраняем хеш токена
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token =
                new EmailVerificationToken(
                        UUID.randomUUID(),
                        user,
                        tokenHash,
                        Instant.now().plus(tokenTtl),
                        Instant.now()
                );

        repository.save(token);

        return rawToken;
    }

    @Transactional
    public void verify(String rawToken) {
        if (rawToken == null || rawToken.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token =
                repository.findByTokenHash(tokenHash)
                        .orElseThrow(
                                InvalidCredentialsException::new
                        );

        if (token.isExpired()) {
            throw new InvalidCredentialsException();
        }

        // Подтверждаем email
        token.getUser().verifyEmail();

        // Удаляем использованный токен
        repository.delete(token);
    }

    private String hashToken(String value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            value.getBytes(StandardCharsets.UTF_8)
                    );

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    exception
            );
        }
    }
}