package pl.exceptionhandled.taskmanager.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pl.exceptionhandled.taskmanager.dto.CreateTaskRequest;
import pl.exceptionhandled.taskmanager.dto.TaskResponse;
import pl.exceptionhandled.taskmanager.dto.UpdateTaskRequest;
import pl.exceptionhandled.taskmanager.dto.UpdateTaskStatusRequest;
import pl.exceptionhandled.taskmanager.entity.Project;
import pl.exceptionhandled.taskmanager.entity.Task;
import pl.exceptionhandled.taskmanager.entity.TaskPriority;
import pl.exceptionhandled.taskmanager.entity.TaskStatus;
import pl.exceptionhandled.taskmanager.entity.User;
import pl.exceptionhandled.taskmanager.exception.ProjectNotFoundException;
import pl.exceptionhandled.taskmanager.exception.TaskNotFoundException;
import pl.exceptionhandled.taskmanager.exception.UserIsNotAssignedException;
import pl.exceptionhandled.taskmanager.exception.UserNotFoundException;
import pl.exceptionhandled.taskmanager.mapper.TaskMapper;
import pl.exceptionhandled.taskmanager.repository.ProjectRepository;
import pl.exceptionhandled.taskmanager.repository.TaskRepository;
import pl.exceptionhandled.taskmanager.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    ProjectRepository projectRepository;

    @Mock
    TaskMapper taskMapper;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    TaskService taskService;

    @Test
    void create_shouldCreateTaskWithTodoStatus() {
        // given
        var projectId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var project = Project.builder()
                .id(projectId)
                .name("Test project")
                .build();

        var request = new CreateTaskRequest(
                "New task",
                "Description",
                TaskPriority.HIGH
        );

        var response = buildTaskResponse(TaskStatus.TODO);

        when(projectRepository.findByIdAndOwnerEmail(projectId, ownerEmail))
                .thenReturn(Optional.of(project));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(taskMapper.taskToResponse(any(Task.class)))
                .thenReturn(response);

        // when
        var result = taskService.create(
                projectId,
                request,
                ownerEmail
        );

        // then
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).save(captor.capture());

        var savedTask = captor.getValue();

        assertThat(savedTask.getTitle()).isEqualTo("New task");
        assertThat(savedTask.getDescription()).isEqualTo("Description");
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(savedTask.getProject()).isSameAs(project);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void create_shouldThrowWhenProjectDoesNotExist() {
        // given
        var projectId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var request = new CreateTaskRequest(
                "Task",
                "Description",
                TaskPriority.MEDIUM
        );

        when(projectRepository.findByIdAndOwnerEmail(projectId, ownerEmail))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                taskService.create(projectId, request, ownerEmail)
        ).isInstanceOf(ProjectNotFoundException.class);

        verify(taskRepository, never()).save(any());
        verifyNoInteractions(taskMapper);
    }

    @Test
    void findAll_shouldReturnMappedPageWithFilters() {
        // given
        var projectId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var project = Project.builder()
                .id(projectId)
                .build();

        var task1 = Task.builder()
                .id(UUID.randomUUID())
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .build();

        var task2 = Task.builder()
                .id(UUID.randomUUID())
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .build();

        var response1 = buildTaskResponse(TaskStatus.TODO);
        var response2 = buildTaskResponse(TaskStatus.TODO);

        var pageable = PageRequest.of(0, 20);

        when(projectRepository.findByIdAndOwnerEmail(projectId, ownerEmail))
                .thenReturn(Optional.of(project));

        when(taskRepository.findAllByFilters(
                projectId,
                ownerEmail,
                TaskStatus.TODO,
                TaskPriority.HIGH,
                pageable
        )).thenReturn(new PageImpl<>(
                List.of(task1, task2),
                pageable,
                2
        ));

        when(taskMapper.taskToResponse(task1))
                .thenReturn(response1);

        when(taskMapper.taskToResponse(task2))
                .thenReturn(response2);

        // when
        var result = taskService.findAll(
                projectId,
                ownerEmail,
                TaskStatus.TODO,
                TaskPriority.HIGH,
                pageable
        );

        // then
        assertThat(result.getContent())
                .containsExactly(response1, response2);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(20);

        verify(taskRepository).findAllByFilters(
                projectId,
                ownerEmail,
                TaskStatus.TODO,
                TaskPriority.HIGH,
                pageable
        );
    }

    @Test
    void findById_shouldThrowWhenTaskDoesNotExist() {
        // given
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        when(taskRepository.findByIdAndProjectIdAndProjectOwnerEmail(
                taskId,
                projectId,
                ownerEmail
        )).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                taskService.findById(
                        projectId,
                        taskId,
                        ownerEmail
                )
        ).isInstanceOf(TaskNotFoundException.class);

        verifyNoInteractions(taskMapper);
    }

    @Test
    void update_shouldUpdateTaskFields() {
        // given
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var task = Task.builder()
                .id(taskId)
                .title("Old title")
                .description("Old description")
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .build();

        var request = new UpdateTaskRequest(
                "New title",
                "New description",
                TaskPriority.HIGH
        );

        var response = buildTaskResponse(TaskStatus.TODO);

        when(taskRepository.findByIdAndProjectIdAndProjectOwnerEmail(
                taskId,
                projectId,
                ownerEmail
        )).thenReturn(Optional.of(task));

        when(taskMapper.taskToResponse(task))
                .thenReturn(response);

        // when
        var result = taskService.update(
                projectId,
                taskId,
                request,
                ownerEmail
        );

        // then
        assertThat(task.getTitle()).isEqualTo("New title");
        assertThat(task.getDescription()).isEqualTo("New description");
        assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);

        assertThat(result).isEqualTo(response);

        verify(taskRepository).flush();
        verify(taskMapper).taskToResponse(task);
    }

    @Test
    void changeStatus_shouldChangeTaskStatus() {
        // given
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var task = Task.builder()
                .id(taskId)
                .status(TaskStatus.TODO)
                .build();

        var request = new UpdateTaskStatusRequest(
                TaskStatus.IN_PROGRESS
        );

        var response = buildTaskResponse(
                TaskStatus.IN_PROGRESS
        );

        when(taskRepository.findByIdAndProjectIdAndProjectOwnerEmail(
                taskId,
                projectId,
                ownerEmail
        )).thenReturn(Optional.of(task));

        when(taskMapper.taskToResponse(any(Task.class)))
                .thenReturn(response);

        // when
        var result = taskService.changeStatus(
                projectId,
                taskId,
                request,
                ownerEmail
        );

        // then
        ArgumentCaptor<Task> captor =
                ArgumentCaptor.forClass(Task.class);

        verify(taskMapper)
                .taskToResponse(captor.capture());

        var capturedTask = captor.getValue();

        assertThat(capturedTask.getStatus())
                .isEqualTo(TaskStatus.IN_PROGRESS);

        assertThat(result)
                .isEqualTo(response);

        verify(taskRepository).flush();
    }

    @Test
    void delete_shouldDeleteExistingTask() {
        // given
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var task = Task.builder()
                .id(taskId)
                .build();

        when(taskRepository.findByIdAndProjectIdAndProjectOwnerEmail(
                taskId,
                projectId,
                ownerEmail
        )).thenReturn(Optional.of(task));

        // when
        taskService.delete(
                projectId,
                taskId,
                ownerEmail
        );

        // then
        verify(taskRepository).delete(task);
    }

    @Test
    void assignUser_shouldAssignUserWhenNotAlreadyAssigned() {
        // given
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var task = Task.builder()
                .id(taskId)
                .status(TaskStatus.TODO)
                .build();

        var user = new User();
        user.setId(userId);

        var response = buildTaskResponse(TaskStatus.TODO);

        when(taskRepository.findByIdAndProjectIdAndProjectOwnerEmail(
                taskId,
                projectId,
                ownerEmail
        )).thenReturn(Optional.of(task));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(taskMapper.taskToResponse(any(Task.class)))
                .thenReturn(response);

        // when
        var result = taskService.assignUser(
                projectId,
                taskId,
                userId,
                ownerEmail
        );

        // then
        ArgumentCaptor<Task> captor =
                ArgumentCaptor.forClass(Task.class);

        verify(taskMapper)
                .taskToResponse(captor.capture());

        var capturedTask = captor.getValue();

        assertThat(capturedTask.getAssignees())
                .contains(user);

        assertThat(capturedTask.getUpdatedAt())
                .isNotNull();

        assertThat(result)
                .isEqualTo(response);

        verify(taskRepository).flush();
    }

    @Test
    void assignUser_shouldNotModifyTaskWhenUserAlreadyAssigned() {
        // given
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var user = new User();
        user.setId(userId);

        var originalUpdatedAt =
                Instant.parse("2026-01-01T10:00:00Z");

        var task = Task.builder()
                .id(taskId)
                .status(TaskStatus.TODO)
                .updatedAt(originalUpdatedAt)
                .assignees(Set.of(user))
                .build();

        var response = buildTaskResponse(TaskStatus.TODO);

        when(taskRepository.findByIdAndProjectIdAndProjectOwnerEmail(
                taskId,
                projectId,
                ownerEmail
        )).thenReturn(Optional.of(task));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(taskMapper.taskToResponse(task))
                .thenReturn(response);

        // when
        var result = taskService.assignUser(
                projectId,
                taskId,
                userId,
                ownerEmail
        );

        // then
        assertThat(task.getAssignees())
                .hasSize(1);

        assertThat(task.getUpdatedAt())
                .isEqualTo(originalUpdatedAt);

        assertThat(result)
                .isEqualTo(response);

        verify(taskRepository).flush();
    }

    @Test
    void assignUser_shouldThrowWhenUserDoesNotExist() {
        // given
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var task = Task.builder()
                .id(taskId)
                .build();

        when(taskRepository.findByIdAndProjectIdAndProjectOwnerEmail(
                taskId,
                projectId,
                ownerEmail
        )).thenReturn(Optional.of(task));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                taskService.assignUser(
                        projectId,
                        taskId,
                        userId,
                        ownerEmail
                )
        ).isInstanceOf(UserNotFoundException.class);

        verify(taskRepository, never()).flush();
        verifyNoInteractions(taskMapper);
    }

    @Test
    void unassignUser_shouldRemoveAssignedUser() {
        // given
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var user = new User();
        user.setId(userId);

        var task = Task.builder()
                .id(taskId)
                .status(TaskStatus.TODO)
                .assignees(new java.util.HashSet<>(Set.of(user)))
                .updatedAt(
                        Instant.parse("2026-01-01T10:00:00Z")
                )
                .build();

        var response = buildTaskResponse(TaskStatus.TODO);

        when(taskRepository.findByIdAndProjectIdAndProjectOwnerEmail(
                taskId,
                projectId,
                ownerEmail
        )).thenReturn(Optional.of(task));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(taskMapper.taskToResponse(task))
                .thenReturn(response);

        // when
        var result = taskService.unassignUser(
                projectId,
                taskId,
                userId,
                ownerEmail
        );

        // then
        assertThat(task.getAssignees())
                .isEmpty();

        assertThat(task.getUpdatedAt())
                .isAfter(
                        Instant.parse("2026-01-01T10:00:00Z")
                );

        assertThat(result)
                .isEqualTo(response);

        verify(taskRepository).flush();
        verify(taskMapper).taskToResponse(task);
    }

    @Test
    void unassignUser_shouldThrowWhenUserIsNotAssigned() {
        // given
        var projectId = UUID.randomUUID();
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var ownerEmail = "owner@test.com";

        var task = Task.builder()
                .id(taskId)
                .build();

        var user = new User();
        user.setId(userId);

        when(taskRepository.findByIdAndProjectIdAndProjectOwnerEmail(
                taskId,
                projectId,
                ownerEmail
        )).thenReturn(Optional.of(task));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when / then
        assertThatThrownBy(() ->
                taskService.unassignUser(
                        projectId,
                        taskId,
                        userId,
                        ownerEmail
                )
        ).isInstanceOf(UserIsNotAssignedException.class);

        verify(taskRepository, never()).flush();
        verifyNoInteractions(taskMapper);
    }

    private TaskResponse buildTaskResponse(TaskStatus status) {
        return new TaskResponse(
                UUID.randomUUID(),
                "Test task",
                "Test description",
                status,
                TaskPriority.MEDIUM,
                UUID.randomUUID(),
                Set.of(),
                Instant.now(),
                Instant.now()
        );
    }
}