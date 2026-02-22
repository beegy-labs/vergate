-- V3: Migrate all primary keys to UUID (UUIDv7 generated in application)
-- Drop existing tables in reverse dependency order

DROP TABLE IF EXISTS legal_documents;
DROP TABLE IF EXISTS remote_configs;
DROP TABLE IF EXISTS notices;
DROP TABLE IF EXISTS maintenances;
DROP TABLE IF EXISTS app_versions;
DROP TABLE IF EXISTS apps;

-- apps: app_key is auto-generated (Base62 of UUIDv7, 22 chars)
CREATE TABLE apps (
    id          UUID         PRIMARY KEY,
    app_key     VARCHAR(22)  NOT NULL UNIQUE,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    platform    VARCHAR(20)  NOT NULL,
    store_url   VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE app_versions (
    id              UUID         PRIMARY KEY,
    app_id          UUID         NOT NULL REFERENCES apps(id) ON DELETE CASCADE,
    min_version     VARCHAR(20)  NOT NULL,
    latest_version  VARCHAR(20)  NOT NULL,
    force_update    BOOLEAN      NOT NULL DEFAULT FALSE,
    update_message  TEXT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_versions_app_id ON app_versions(app_id);

CREATE TABLE maintenances (
    id          UUID         PRIMARY KEY,
    app_id      UUID         NOT NULL REFERENCES apps(id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    message     TEXT,
    start_at    TIMESTAMPTZ  NOT NULL,
    end_at      TIMESTAMPTZ  NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_maintenances_app_id ON maintenances(app_id);

CREATE TABLE notices (
    id            UUID         PRIMARY KEY,
    app_id        UUID         NOT NULL REFERENCES apps(id) ON DELETE CASCADE,
    title         VARCHAR(200) NOT NULL,
    message       TEXT,
    image_url     VARCHAR(500),
    deep_link     VARCHAR(500),
    display_type  VARCHAR(20)  NOT NULL DEFAULT 'ONCE',
    priority      INT          NOT NULL DEFAULT 0,
    start_at      TIMESTAMPTZ,
    end_at        TIMESTAMPTZ,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notices_app_id ON notices(app_id);

CREATE TABLE remote_configs (
    id           UUID         PRIMARY KEY,
    app_id       UUID         NOT NULL REFERENCES apps(id) ON DELETE CASCADE,
    config_key   VARCHAR(100) NOT NULL,
    config_value TEXT         NOT NULL,
    value_type   VARCHAR(20)  NOT NULL DEFAULT 'STRING',
    description  TEXT,
    ab_ratio     INT          NOT NULL DEFAULT 100,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(app_id, config_key)
);

CREATE INDEX idx_remote_configs_app_id ON remote_configs(app_id);

CREATE TABLE legal_documents (
    id           UUID         PRIMARY KEY,
    app_id       UUID         NOT NULL REFERENCES apps(id) ON DELETE CASCADE,
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
