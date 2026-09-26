package pl.exceptionhandled.taskmanager.dto;

import pl.exceptionhandled.taskmanager.entity.ProjectRole;

import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID id,
        UUID userId,
        String email,
        ProjectRole role,
        Instant createdAt
) {
}