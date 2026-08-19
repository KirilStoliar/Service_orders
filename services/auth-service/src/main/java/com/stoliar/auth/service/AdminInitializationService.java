package com.stoliar.auth.service;

import com.stoliar.auth.entity.AuthUser;
import com.stoliar.auth.model.Role;
import com.stoliar.auth.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class AdminInitializationService implements ApplicationRunner {

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@service-orders.com}")
    private String adminEmail;

    @Value("${admin.password:Admin123!}")
    private String adminPassword;

    @Value("${admin.enabled:true}")
    private boolean adminEnabled;

    public AdminInitializationService(
            AuthUserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createAdminIfNotExists();
    }

    private void createAdminIfNotExists() {
        if (!adminEnabled) {
            System.out.println("⚠️ Admin auto-creation is disabled");
            return;
        }

        // Проверяем, существует ли уже администратор
        boolean adminExists = userRepository.findByEmailIgnoreCase(adminEmail).isPresent();

        if (adminExists) {
            System.out.println("✅ Admin user already exists: " + adminEmail);
            return;
        }

        // Создаём администратора
        AuthUser admin = new AuthUser(
                UUID.randomUUID(),
                adminEmail,
                passwordEncoder.encode(adminPassword),
                Role.ROLE_ADMIN,
                true,
                true, // Сразу верифицирован
                Instant.now()
        );

        userRepository.save(admin);

        System.out.println("✅ Admin user created successfully!");
        System.out.println("   Email: " + adminEmail);
        System.out.println("   Password: " + adminPassword);
        System.out.println("   Role: ADMIN");
    }
}