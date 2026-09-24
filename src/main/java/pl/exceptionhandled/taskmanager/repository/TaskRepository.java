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

    Optional<Task> findByIdAndProjectIdAndProjectOwnerEmail(
            UUID taskId,
            UUID projectId,
            String ownerEmail
    );

    @EntityGraph(attributePaths = {"assignees"})
    @Query("""
            SELECT t
            FROM Task t
            WHERE t.id = :taskId
              AND t.project.id = :projectId
              AND t.project.owner.email = :ownerEmail
            """)
    Optional<Task> findWithAssignees(
            @Param("taskId") UUID taskId,
            @Param("projectId") UUID projectId,
            @Param("ownerEmail") String ownerEmail
    );

    @Query(
            value = """
                    SELECT t
                    FROM Task t
                    WHERE t.project.id = :projectId
                      AND t.project.owner.email = :ownerEmail
                      AND (:status IS NULL OR t.status = :status)
                      AND (:priority IS NULL OR t.priority = :priority)
                    ORDER BY

                      CASE
                        WHEN :sortBy = 'priority'
                          AND :direction = 'ASC'
                        THEN
                          CASE t.priority
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskPriority.LOW THEN 1
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskPriority.MEDIUM THEN 2
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskPriority.HIGH THEN 3
                          END
                      END ASC,

                      CASE
                        WHEN :sortBy = 'priority'
                          AND :direction = 'DESC'
                        THEN
                          CASE t.priority
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskPriority.LOW THEN 1
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskPriority.MEDIUM THEN 2
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskPriority.HIGH THEN 3
                          END
                      END DESC,

                      CASE
                        WHEN :sortBy = 'status'
                          AND :direction = 'ASC'
                        THEN
                          CASE t.status
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskStatus.TODO THEN 1
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskStatus.IN_PROGRESS THEN 2
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskStatus.DONE THEN 3
                          END
                      END ASC,

                      CASE
                        WHEN :sortBy = 'status'
                          AND :direction = 'DESC'
                        THEN
                          CASE t.status
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskStatus.TODO THEN 1
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskStatus.IN_PROGRESS THEN 2
                            WHEN pl.exceptionhandled.taskmanager.entity.TaskStatus.DONE THEN 3
                          END
                      END DESC,

                      CASE
                        WHEN :sortBy = 'createdAt'
                          AND :direction = 'ASC'
                        THEN t.createdAt
                      END ASC,

                      CASE
                        WHEN :sortBy = 'createdAt'
                          AND :direction = 'DESC'
                        THEN t.createdAt
                      END DESC,

                      t.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(t)
                    FROM Task t
                    WHERE t.project.id = :projectId
                      AND t.project.owner.email = :ownerEmail
                      AND (:status IS NULL OR t.status = :status)
                      AND (:priority IS NULL OR t.priority = :priority)
                    """
    )
    Page<Task> findAllByFilters(
            @Param("projectId") UUID projectId,
            @Param("ownerEmail") String ownerEmail,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("sortBy") String sortBy,
            @Param("direction") String direction,
            Pageable pageable
    );
}