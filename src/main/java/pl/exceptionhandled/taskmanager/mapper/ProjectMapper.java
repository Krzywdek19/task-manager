package pl.exceptionhandled.taskmanager.mapper;

import pl.exceptionhandled.taskmanager.dto.ProjectResponse;
import pl.exceptionhandled.taskmanager.entity.Project;

public interface ProjectMapper {
    ProjectResponse projectToResponse(Project project);
}
