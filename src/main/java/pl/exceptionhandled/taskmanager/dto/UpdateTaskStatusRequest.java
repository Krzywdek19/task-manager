package pl.exceptionhandled.taskmanager.dto;

import jakarta.validation.constraints.NotNull;
import pl.exceptionhandled.taskmanager.entity.TaskStatus;

public record UpdateTaskStatusRequest(@NotNull TaskStatus status) {
}
