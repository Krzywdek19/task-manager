package pl.exceptionhandled.taskmanager.exception;

import java.util.UUID;

public class UserIsNotProjectMemberException extends ApiException {

    public UserIsNotProjectMemberException(
            UUID userId,
            UUID projectId
    ) {
        super(
                "User '%s' is not a member of project '%s'"
                        .formatted(userId, projectId),
                ErrorCode.USER_NOT_PROJECT_MEMBER
        );
    }
}