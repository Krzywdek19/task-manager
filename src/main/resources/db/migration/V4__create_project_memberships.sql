CREATE TABLE project_memberships
(
    id         UUID        PRIMARY KEY,
    project_id UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    role       VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_project_memberships_project
        FOREIGN KEY (project_id)
            REFERENCES projects (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_project_memberships_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_project_memberships_project_user
        UNIQUE (project_id, user_id),

    CONSTRAINT chk_project_memberships_role
        CHECK (role IN ('MEMBER'))
);

CREATE INDEX idx_project_memberships_user_id
    ON project_memberships (user_id);