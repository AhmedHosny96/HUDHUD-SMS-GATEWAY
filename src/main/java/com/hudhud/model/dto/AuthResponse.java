package com.hudhud.model.dto;

public record AuthResponse(
        int status,
        String message,
        String username,
        String token
) {
}
