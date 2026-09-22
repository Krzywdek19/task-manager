package pl.exceptionhandled.taskmanager.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.exceptionhandled.taskmanager.entity.*;
import pl.exceptionhandled.taskmanager.repository.ProjectRepository;
import pl.exceptionhandled.taskmanager.repository.TaskRepository;
import pl.exceptionhandled.taskmanager.repository.UserRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class TaskServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18");

    @Autowired
    TaskService taskService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void assignAndUnassignUser_shouldPersistRelation() {
        // given
        var owner = new User();
        owner.setEmail("owner@test.com");
        owner.setPasswordHash("hash");
        owner.setRoles(Set.of(Role.USER));

        owner = userRepository.saveAndFlush(owner);

        var assignee = new User();
        assignee.setEmail("assignee@test.com");
        assignee.setPasswordHash("hash");
        assignee.setRoles(Set.of(Role.USER));

        assignee = userRepository.saveAndFlush(assignee);

        var project = Project.builder()
                .name("Integration project")
                .description("Test project")
                .owner(owner)
                .build();

        project = projectRepository.saveAndFlush(project);

        var task = Task.builder()
                .title("Integration task")
                .description("Test task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .project(project)
                .build();

        task = taskRepository.saveAndFlush(task);

        var taskId = task.getId();
        var projectId = project.getId();
        var assigneeId = assignee.getId();

        // when - assign
        taskService.assignUser(
                projectId,
                taskId,
                assigneeId,
                owner.getEmail()
        );

        entityManager.clear();

        // then
        var assignedTask = taskRepository
                .findByIdAndProjectIdAndProjectOwnerEmail(
                        taskId,
                        projectId,
                        owner.getEmail()
                )
                .orElseThrow();

        assertThat(assignedTask.getAssignees())
                .extracting(User::getId)
                .containsExactly(assigneeId);

        // when - unassign
        taskService.unassignUser(
                projectId,
                taskId,
                assigneeId,
                owner.getEmail()
        );

        entityManager.clear();

        // then
        var unassignedTask = taskRepository
                .findByIdAndProjectIdAndProjectOwnerEmail(
                        taskId,
                        projectId,
                        owner.getEmail()
                )
                .orElseThrow();

        assertThat(unassignedTask.getAssignees())
                .isEmpty();
    }
}