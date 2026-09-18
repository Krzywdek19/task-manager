package pl.exceptionhandled.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.taskmanager.dto.CreateTaskRequest;
import pl.exceptionhandled.taskmanager.dto.TaskResponse;
import pl.exceptionhandled.taskmanager.dto.UpdateTaskRequest;
import pl.exceptionhandled.taskmanager.entity.Project;
import pl.exceptionhandled.taskmanager.entity.Task;
import pl.exceptionhandled.taskmanager.entity.TaskStatus;
import pl.exceptionhandled.taskmanager.exception.ProjectNotFoundException;
import pl.exceptionhandled.taskmanager.exception.TaskNotFoundException;
import pl.exceptionhandled.taskmanager.mapper.TaskMapper;
import pl.exceptionhandled.taskmanager.repository.ProjectRepository;
import pl.exceptionhandled.taskmanager.repository.TaskRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskResponse create(
            UUID projectId,
            CreateTaskRequest request,
            String ownerEmail
    ) {
        var project = getProjectForOwner(projectId, ownerEmail);

        var task = Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .status(TaskStatus.TODO)
                .project(project)
                .build();

        var savedTask = taskRepository.save(task);

        return taskMapper.taskToResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll(
            UUID projectId,
            String ownerEmail
    ) {
        getProjectForOwner(projectId, ownerEmail);

        return taskRepository
                .findAllByProjectIdAndProjectOwnerEmail(
                        projectId,
                        ownerEmail
                )
                .stream()
                .map(taskMapper::taskToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(
            UUID projectId,
            UUID taskId,
            String ownerEmail
    ) {
        var task = getTaskForOwner(
                taskId,
                projectId,
                ownerEmail
        );

        return taskMapper.taskToResponse(task);
    }

    @Transactional
    public TaskResponse update(
            UUID projectId,
            UUID taskId,
            UpdateTaskRequest request,
            String ownerEmail
    ) {
        var task = getTaskForOwner(
                taskId,
                projectId,
                ownerEmail
        );

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());

        return taskMapper.taskToResponse(task);
    }

    @Transactional
    public void delete(
            UUID projectId,
            UUID taskId,
            String ownerEmail
    ) {
        var task = getTaskForOwner(
                taskId,
                projectId,
                ownerEmail
        );

        taskRepository.delete(task);
    }

    private Task getTaskForOwner(
            UUID taskId,
            UUID projectId,
            String ownerEmail
    ) {
        return taskRepository
                .findByIdAndProjectIdAndProjectOwnerEmail(
                        taskId,
                        projectId,
                        ownerEmail
                )
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private Project getProjectForOwner(
            UUID projectId,
            String ownerEmail
    ) {
        return projectRepository
                .findByIdAndOwnerEmail(
                        projectId,
                        ownerEmail
                )
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}