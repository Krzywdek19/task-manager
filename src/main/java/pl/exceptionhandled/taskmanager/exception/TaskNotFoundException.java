package pl.exceptionhandled.taskmanager.exception;

import java.util.UUID;

public class TaskNotFoundException extends ApiException {

    public TaskNotFoundException(UUID taskId) {
        super(
                "Task with id: %s was not found".formatted(taskId),
                ErrorCode.TASK_NOT_FOUND
        );
    }
}