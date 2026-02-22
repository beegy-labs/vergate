-- Legal documents (privacy policy, terms of service)
CREATE TABLE legal_documents (
    id           BIGSERIAL    PRIMARY KEY,
    app_id       BIGINT       NOT NULL REFERENCES apps(id) ON DELETE CASCADE,
    doc_type     VARCHAR(50)  NOT NULL,
    title        VARCHAR(200) NOT NULL,
    content      TEXT         NOT NULL,
    content_type VARCHAR(20)  NOT NULL DEFAULT 'MARKDOWN',
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(app_id, doc_type)
);

CREATE INDEX idx_legal_documents_app_id ON legal_documents(app_id);
