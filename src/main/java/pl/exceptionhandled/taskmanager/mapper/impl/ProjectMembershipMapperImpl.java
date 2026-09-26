package pl.exceptionhandled.taskmanager.mapper.impl;

import org.springframework.stereotype.Component;
import pl.exceptionhandled.taskmanager.dto.ProjectMemberResponse;
import pl.exceptionhandled.taskmanager.entity.ProjectMembership;
import pl.exceptionhandled.taskmanager.mapper.ProjectMembershipMapper;

@Component
public class ProjectMembershipMapperImpl implements ProjectMembershipMapper {
    @Override
    public ProjectMemberResponse toResponse(ProjectMembership membership) {
        return new ProjectMemberResponse(membership.getId(), membership.getUser().getId(), membership.getUser().getEmail(), membership.getRole(), membership.getCreatedAt());
    }
}
