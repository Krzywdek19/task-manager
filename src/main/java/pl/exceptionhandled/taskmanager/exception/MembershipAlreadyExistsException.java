package pl.exceptionhandled.taskmanager.exception;

import java.util.UUID;

public class MembershipAlreadyExistsException extends ApiException {

    public MembershipAlreadyExistsException(String email, UUID projectId) {
        super(
                "User with email '%s' is already a member of project with ID '%s'"
                        .formatted(email, projectId),
                ErrorCode.MEMBERSHIP_ALREADY_EXISTS
        );
    }
}