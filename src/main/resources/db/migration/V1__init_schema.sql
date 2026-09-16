CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       hashed_password VARCHAR(255) NOT NULL,

                       CONSTRAINT uq_users_email UNIQUE (email)
);


CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role VARCHAR(255) NOT NULL,

                            CONSTRAINT pk_user_roles
                                PRIMARY KEY (user_id, role),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users (id)
                                    ON DELETE CASCADE,

                            CONSTRAINT chk_user_roles_role
                                CHECK (role IN ('USER', 'ADMIN'))
);


CREATE TABLE projects (
                          id UUID PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          description VARCHAR(255),
                          owner_id UUID NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                          CONSTRAINT fk_projects_owner
                              FOREIGN KEY (owner_id)
                                  REFERENCES users (id)
);


CREATE TABLE tasks (
                       id UUID PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description VARCHAR(255),
                       status VARCHAR(255) NOT NULL,
                       priority VARCHAR(255) NOT NULL,
                       project_id UUID NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                       CONSTRAINT fk_tasks_project
                           FOREIGN KEY (project_id)
                               REFERENCES projects (id),

                       CONSTRAINT chk_tasks_status
                           CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),

                       CONSTRAINT chk_tasks_priority
                           CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);


CREATE TABLE user_tasks (
                            task_id UUID NOT NULL,
                            user_id UUID NOT NULL,

                            CONSTRAINT pk_user_tasks
                                PRIMARY KEY (task_id, user_id),

                            CONSTRAINT fk_user_tasks_task
                                FOREIGN KEY (task_id)
                                    REFERENCES tasks (id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_user_tasks_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users (id)
                                    ON DELETE CASCADE
);


CREATE INDEX idx_projects_owner_id
    ON projects (owner_id);

CREATE INDEX idx_tasks_project_id
    ON tasks (project_id);

CREATE INDEX idx_user_tasks_user_id
    ON user_tasks (user_id);