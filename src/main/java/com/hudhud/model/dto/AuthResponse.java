package com.hudhud.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AuthResponse(
        int status,
        String message,
        String username,
        String token
) {
}
