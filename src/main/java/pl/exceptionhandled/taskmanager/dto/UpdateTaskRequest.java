package pl.exceptionhandled.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.exceptionhandled.taskmanager.entity.TaskPriority;

public record UpdateTaskRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 255)
        String description,

        @NotNull
        TaskPriority priority
) {
}