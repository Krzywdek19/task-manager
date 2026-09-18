package pl.exceptionhandled.taskmanager.dto;

import pl.exceptionhandled.taskmanager.entity.TaskPriority;
import pl.exceptionhandled.taskmanager.entity.TaskStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UUID projectId,
        Set<UUID> assigneeIds,
        Instant createdAt,
        Instant updatedAt
) {
}