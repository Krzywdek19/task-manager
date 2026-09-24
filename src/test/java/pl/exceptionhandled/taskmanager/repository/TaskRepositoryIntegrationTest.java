package pl.exceptionhandled.taskmanager.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pl.exceptionhandled.taskmanager.entity.Project;
import pl.exceptionhandled.taskmanager.entity.Task;
import pl.exceptionhandled.taskmanager.entity.TaskPriority;
import pl.exceptionhandled.taskmanager.entity.TaskStatus;
import pl.exceptionhandled.taskmanager.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class TaskRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18");

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRepository userRepository;

    private User owner;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setEmail("owner@test.com");
        owner.setPasswordHash("hash");

        owner = userRepository.saveAndFlush(owner);

        project = Project.builder()
                .name("Integration project")
                .description("Project for repository tests")
                .owner(owner)
                .build();

        project = projectRepository.saveAndFlush(project);
    }

    @Test
    void findAllByFilters_shouldSortPriorityByDomainOrderDescending() {
        // given
        saveTask("Low priority", TaskStatus.TODO, TaskPriority.LOW);
        saveTask("High priority", TaskStatus.TODO, TaskPriority.HIGH);
        saveTask("Medium priority", TaskStatus.TODO, TaskPriority.MEDIUM);

        var pageable = PageRequest.of(0, 20);

        // when
        var result = taskRepository.findAllByFilters(
                project.getId(),
                owner.getEmail(),
                null,
                null,
                "priority",
                "DESC",
                pageable
        );

        // then
        assertThat(result.getContent())
                .extracting(Task::getPriority)
                .containsExactly(
                        TaskPriority.HIGH,
                        TaskPriority.MEDIUM,
                        TaskPriority.LOW
                );
    }

    @Test
    void findAllByFilters_shouldSortStatusByDomainOrderAscending() {
        // given
        saveTask("Done task", TaskStatus.DONE, TaskPriority.MEDIUM);
        saveTask("Todo task", TaskStatus.TODO, TaskPriority.MEDIUM);
        saveTask(
                "In progress task",
                TaskStatus.IN_PROGRESS,
                TaskPriority.MEDIUM
        );

        var pageable = PageRequest.of(0, 20);

        // when
        var result = taskRepository.findAllByFilters(
                project.getId(),
                owner.getEmail(),
                null,
                null,
                "status",
                "ASC",
                pageable
        );

        // then
        assertThat(result.getContent())
                .extracting(Task::getStatus)
                .containsExactly(
                        TaskStatus.TODO,
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.DONE
                );
    }

    private Task saveTask(
            String title,
            TaskStatus status,
            TaskPriority priority
    ) {
        var task = Task.builder()
                .title(title)
                .description("Integration test task")
                .status(status)
                .priority(priority)
                .project(project)
                .build();

        return taskRepository.saveAndFlush(task);
    }
}