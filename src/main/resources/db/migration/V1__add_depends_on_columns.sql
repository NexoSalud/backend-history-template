-- Migration: Agregar columnas depends_on_attr_id y depends_on_value
-- a la tabla fb_template_group_attr si no existen.
-- Estas columnas son necesarias para la lógica condicional de visibilidad
-- de campos (dependencia entre atributos).

ALTER TABLE fb_template_group_attr
    ADD COLUMN IF NOT EXISTS depends_on_attr_id   BIGINT REFERENCES fb_attribute(id),
    ADD COLUMN IF NOT EXISTS depends_on_value     VARCHAR(500);
