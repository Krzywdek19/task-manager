package pl.exceptionhandled.taskmanager.exception;

import java.util.UUID;

public class ProjectNotFoundException extends ApiException {

    public ProjectNotFoundException(UUID projectId) {
        super(
                "Project with id: %s was not found".formatted(projectId),
                ErrorCode.PROJECT_NOT_FOUND
        );
    }
}