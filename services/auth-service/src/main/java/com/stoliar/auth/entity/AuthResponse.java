package com.stoliar.auth.entity;

public record AuthResponse(

        String accessToken,

        String refreshToken,

        long expiresIn,

        String tokenType
) {
}