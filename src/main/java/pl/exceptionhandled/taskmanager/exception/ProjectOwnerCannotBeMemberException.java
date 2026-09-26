package pl.exceptionhandled.taskmanager.exception;

public class ProjectOwnerCannotBeMemberException extends ApiException{
    public ProjectOwnerCannotBeMemberException() {
        super("Project owner cannot be member", ErrorCode.PROJECT_OWNER_CANNOT_BE_MEMBER);
    }
}
