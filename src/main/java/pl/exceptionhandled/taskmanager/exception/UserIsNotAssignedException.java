package pl.exceptionhandled.taskmanager.exception;


import java.util.UUID;

public class UserIsNotAssignedException extends ApiException {

    public UserIsNotAssignedException(UUID userId, UUID taskId) {
        super(
                "User with id: %s is not assigned to task with id: %s"
                        .formatted(userId, taskId),
                ErrorCode.USER_NOT_ASSIGNED
        );
    }
}