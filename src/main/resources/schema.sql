CREATE TABLE IF NOT EXISTS fb_attribute (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    label           VARCHAR(255) NOT NULL,
    input_type      VARCHAR(30) NOT NULL,
    is_required     BOOLEAN NOT NULL DEFAULT FALSE,
    placeholder     VARCHAR(255),
    default_value   VARCHAR(500),
    tooltip         VARCHAR(500),
    validation_rules JSON,
    options         JSON,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fb_template (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    version     INTEGER NOT NULL DEFAULT 1,
    scope       VARCHAR(50) NOT NULL DEFAULT 'CONSULTA_EXTERNA',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fb_template_group (
    id              BIGSERIAL PRIMARY KEY,
    template_id     BIGINT NOT NULL REFERENCES fb_template(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    icon            VARCHAR(50),
    is_collapsible  BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_tg_template ON fb_template_group(template_id);

CREATE TABLE IF NOT EXISTS fb_template_group_attr (
    id                   BIGSERIAL PRIMARY KEY,
    group_id             BIGINT NOT NULL REFERENCES fb_template_group(id) ON DELETE CASCADE,
    attribute_id         BIGINT NOT NULL REFERENCES fb_attribute(id) ON DELETE RESTRICT,
    sort_order           INTEGER NOT NULL DEFAULT 0,
    is_required_override BOOLEAN,
    label_override       VARCHAR(255),
    width                VARCHAR(10) NOT NULL DEFAULT 'full',
    depends_on_attr_id   BIGINT REFERENCES fb_attribute(id),
    depends_on_value     VARCHAR(500),

    UNIQUE (group_id, attribute_id)
);

CREATE INDEX IF NOT EXISTS idx_tga_group ON fb_template_group_attr(group_id);
CREATE INDEX IF NOT EXISTS idx_tga_attribute ON fb_template_group_attr(attribute_id);
