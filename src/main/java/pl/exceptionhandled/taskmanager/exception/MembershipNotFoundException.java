package pl.exceptionhandled.taskmanager.exception;

import java.util.UUID;

public class MembershipNotFoundException extends ApiException {

    public MembershipNotFoundException(
            UUID userId,
            UUID projectId
    ) {
        super(
                "User '%s' is not a member of project '%s'"
                        .formatted(userId, projectId),
                ErrorCode.MEMBERSHIP_NOT_FOUND
        );
    }
}