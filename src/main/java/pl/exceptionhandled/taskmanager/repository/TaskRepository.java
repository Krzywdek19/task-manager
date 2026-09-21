package pl.exceptionhandled.taskmanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.exceptionhandled.taskmanager.entity.Task;
import pl.exceptionhandled.taskmanager.entity.TaskPriority;
import pl.exceptionhandled.taskmanager.entity.TaskStatus;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @EntityGraph(attributePaths = {"assignees"})
    Optional<Task> findByIdAndProjectIdAndProjectOwnerEmail(
            UUID taskId,
            UUID projectId,
            String ownerEmail
    );

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.project.id = :projectId
              AND t.project.owner.email = :ownerEmail
              AND (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
            """)
    Page<Task> findAllByFilters(
            @Param("projectId") UUID projectId,
            @Param("ownerEmail") String ownerEmail,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            Pageable pageable
    );
}