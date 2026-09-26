package pl.exceptionhandled.taskmanager.mapper;

import pl.exceptionhandled.taskmanager.dto.ProjectMemberResponse;
import pl.exceptionhandled.taskmanager.entity.ProjectMembership;

public interface ProjectMembershipMapper {
    ProjectMemberResponse toResponse(ProjectMembership membership);
}
