package pl.exceptionhandled.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.taskmanager.dto.CreateTaskRequest;
import pl.exceptionhandled.taskmanager.dto.TaskResponse;
import pl.exceptionhandled.taskmanager.dto.UpdateTaskRequest;
import pl.exceptionhandled.taskmanager.dto.UpdateTaskStatusRequest;
import pl.exceptionhandled.taskmanager.entity.Project;
import pl.exceptionhandled.taskmanager.entity.Task;
import pl.exceptionhandled.taskmanager.entity.TaskPriority;
import pl.exceptionhandled.taskmanager.entity.TaskStatus;
import pl.exceptionhandled.taskmanager.exception.*;
import pl.exceptionhandled.taskmanager.mapper.TaskMapper;
import pl.exceptionhandled.taskmanager.repository.ProjectMembershipRepository;
import pl.exceptionhandled.taskmanager.repository.ProjectRepository;
import pl.exceptionhandled.taskmanager.repository.TaskRepository;
import pl.exceptionhandled.taskmanager.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final UserRepository userRepository;

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
    public Page<TaskResponse> findAll(
            UUID projectId,
            String ownerEmail,
            TaskStatus status,
            TaskPriority priority,
            Pageable pageable
    ) {
        getProjectForOwner(projectId, ownerEmail);

        Sort.Order order = pageable.getSort()
                .stream()
                .findFirst()
                .orElse(Sort.Order.desc("createdAt"));

        String sortBy = validateSortProperty(order.getProperty());
        String direction = order.getDirection().name();

        Pageable unsortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return taskRepository
                .findAllByFilters(
                        projectId,
                        ownerEmail,
                        status,
                        priority,
                        sortBy,
                        direction,
                        unsortedPageable
                )
                .map(taskMapper::taskToResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(
            UUID projectId,
            UUID taskId,
            String ownerEmail
    ) {
        var task = getTaskForOwnerWithAssignees(
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
        var task = getTaskForOwnerWithAssignees(
                taskId,
                projectId,
                ownerEmail
        );

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());

        taskRepository.flush();

        return taskMapper.taskToResponse(task);
    }

    @Transactional
    public TaskResponse changeStatus(
            UUID projectId,
            UUID taskId,
            UpdateTaskStatusRequest request,
            String ownerEmail
    ) {
        var task = getTaskForOwnerWithAssignees(
                taskId,
                projectId,
                ownerEmail
        );

        task.setStatus(request.status());

        taskRepository.flush();

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

    @Transactional
    public TaskResponse assignUser(
            UUID projectId,
            UUID taskId,
            UUID userId,
            String ownerEmail
    ) {
        var task = getTaskForOwnerWithAssignees(
                taskId,
                projectId,
                ownerEmail
        );

        var user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean isProjectOwner =
                user.getEmail().equalsIgnoreCase(ownerEmail);

        boolean isProjectMember =
                projectMembershipRepository
                        .existsByProjectIdAndUserId(
                                projectId,
                                userId
                        );

        if (!isProjectOwner && !isProjectMember) {
            throw new UserIsNotProjectMemberException(
                    userId,
                    projectId
            );
        }

        boolean assigned = task.getAssignees()
                .stream()
                .anyMatch(assignee -> assignee.getId().equals(userId));

        if (!assigned) {
            task.getAssignees().add(user);
            task.touch();
        }

        taskRepository.flush();

        return taskMapper.taskToResponse(task);
    }

    @Transactional
    public TaskResponse unassignUser(
            UUID projectId,
            UUID taskId,
            UUID userId,
            String ownerEmail
    ) {
        var task = getTaskForOwnerWithAssignees(
                taskId,
                projectId,
                ownerEmail
        );

        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean removed = task.getAssignees()
                .removeIf(assignee -> assignee.getId().equals(userId));

        if (!removed) {
            throw new UserIsNotAssignedException(userId, taskId);
        }

        task.touch();

        taskRepository.flush();

        return taskMapper.taskToResponse(task);
    }

    private String validateSortProperty(String property) {
        return switch (property) {
            case "priority", "status", "createdAt" -> property;
            default -> "createdAt";
        };
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

    private Task getTaskForOwnerWithAssignees(
            UUID taskId,
            UUID projectId,
            String ownerEmail
    ) {
        return taskRepository
                .findWithAssignees(
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