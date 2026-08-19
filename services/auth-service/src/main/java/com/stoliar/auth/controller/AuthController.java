package com.stoliar.auth.controller;

import com.stoliar.auth.dto.*;
import com.stoliar.auth.entity.AuthResponse;
import com.stoliar.auth.entity.MessageResponse;
import com.stoliar.auth.exception.InvalidCredentialsException;
import com.stoliar.auth.service.AuthService;
import com.stoliar.auth.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and Authorization endpoints")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService verificationService;

    public AuthController(
            AuthService authService,
            EmailVerificationService verificationService
    ) {
        this.authService = authService;
        this.verificationService = verificationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public MessageResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login to the system")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public AuthResponse refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authService.refresh(
                request.refreshToken()
        );
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public MessageResponse logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authService.logout(
                request.refreshToken()
        );
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify email address")
    public MessageResponse verify(
            @RequestBody VerifyTokenRequest request
    ) {
        verificationService.verify(request.token());

        return new MessageResponse(
                "Email successfully verified"
        );
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user info",
            description = "Returns information about the currently authenticated user including roles",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public UserInfoResponse getCurrentUser(
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidCredentialsException();
        }

        // Получаем JWT токен
        Jwt jwt = (Jwt) authentication.getPrincipal();

        // Получаем userId из subject
        String userId = jwt.getSubject();

        // Получаем email из claims
        String email = jwt.getClaim("email");

        // Получаем roles - они могут быть как String, так и List
        Object rolesObj = jwt.getClaim("roles");
        List<String> roles;

        if (rolesObj instanceof List) {
            // Если roles уже список
            @SuppressWarnings("unchecked")
            List<String> rolesList = (List<String>) rolesObj;
            roles = rolesList;
        } else if (rolesObj instanceof String) {
            // Если roles строка - преобразуем в список
            roles = List.of((String) rolesObj);
        } else {
            roles = List.of();
        }

        return new UserInfoResponse(
                userId,
                email,
                authentication.getName(),
                roles,
                authentication.isAuthenticated()
        );
    }
}