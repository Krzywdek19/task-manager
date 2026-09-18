package pl.exceptionhandled.taskmanager.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.exceptionhandled.taskmanager.entity.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @EntityGraph(attributePaths = {"assignees"})
    List<Task> findAllByProjectIdAndProjectOwnerEmail(
            UUID projectId,
            String ownerEmail
    );

    @EntityGraph(attributePaths = {"assignees"})
    Optional<Task> findByIdAndProjectIdAndProjectOwnerEmail(
            UUID taskId,
            UUID projectId,
            String ownerEmail
    );
}