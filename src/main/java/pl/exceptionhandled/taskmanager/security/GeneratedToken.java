package pl.exceptionhandled.taskmanager.security;

import java.time.Instant;

public record GeneratedToken(
        String value,
        Instant expiresAt
) {
}