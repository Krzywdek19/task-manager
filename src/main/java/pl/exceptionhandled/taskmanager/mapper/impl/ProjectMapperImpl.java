package pl.exceptionhandled.taskmanager.mapper.impl;

import org.springframework.stereotype.Component;
import pl.exceptionhandled.taskmanager.dto.ProjectResponse;
import pl.exceptionhandled.taskmanager.entity.Project;
import pl.exceptionhandled.taskmanager.mapper.ProjectMapper;

@Component
public class ProjectMapperImpl implements ProjectMapper {
    @Override
    public ProjectResponse projectToResponse(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), project.getOwner().getId(), project.getCreatedAt());
    }
}
