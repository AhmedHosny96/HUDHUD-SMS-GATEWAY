package com.hudhud.model.dto;

public record TokenBody(
        String username, Long clientId, Integer status
) {
}
