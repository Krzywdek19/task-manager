package pl.exceptionhandled.taskmanager.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Table(name = "tasks")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = true)
    private String description;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TaskStatus status;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TaskPriority priority;
    @JoinColumn(nullable = false, name = "project_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> assignees = new HashSet<>();
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
