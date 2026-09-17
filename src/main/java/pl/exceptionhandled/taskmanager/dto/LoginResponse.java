package pl.exceptionhandled.taskmanager.dto;

import java.time.Instant;

public record LoginResponse(String jwt, Instant expiresAt) {
}
