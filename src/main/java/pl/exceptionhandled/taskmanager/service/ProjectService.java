package pl.exceptionhandled.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.taskmanager.dto.CreateProjectRequest;
import pl.exceptionhandled.taskmanager.dto.ProjectResponse;
import pl.exceptionhandled.taskmanager.dto.UpdateProjectRequest;
import pl.exceptionhandled.taskmanager.entity.Project;
import pl.exceptionhandled.taskmanager.entity.User;
import pl.exceptionhandled.taskmanager.exception.ProjectNotFoundException;
import pl.exceptionhandled.taskmanager.exception.UserNotFoundException;
import pl.exceptionhandled.taskmanager.mapper.ProjectMapper;
import pl.exceptionhandled.taskmanager.repository.ProjectRepository;
import pl.exceptionhandled.taskmanager.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectResponse create(
            CreateProjectRequest request,
            String ownerEmail
    ) {
        var user = getUserByEmail(ownerEmail);

        var project = Project.builder()
                .name(request.name())
                .description(request.description())
                .owner(user)
                .build();

        var savedProject = projectRepository.save(project);

        return projectMapper.projectToResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAllForUser(String ownerEmail) {
        return projectRepository.findAllByOwnerEmail(ownerEmail)
                .stream()
                .map(projectMapper::projectToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(
            UUID projectId,
            String ownerEmail
    ) {
        var project = getProjectForOwner(projectId, ownerEmail);

        return projectMapper.projectToResponse(project);
    }

    @Transactional
    public ProjectResponse update(
            UUID projectId,
            UpdateProjectRequest request,
            String ownerEmail
    ) {
        var project = getProjectForOwner(projectId, ownerEmail);

        project.setName(request.name());
        project.setDescription(request.description());

        return projectMapper.projectToResponse(project);
    }

    @Transactional
    public void delete(
            UUID projectId,
            String ownerEmail
    ) {
        var project = getProjectForOwner(projectId, ownerEmail);

        projectRepository.delete(project);
    }

    private Project getProjectForOwner(
            UUID projectId,
            String ownerEmail
    ) {
        return projectRepository.findByIdAndOwnerEmail(projectId, ownerEmail)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}