# Form Builder - Guia de Implementacion Backend

## Resumen

Este documento describe las tablas, relaciones y endpoints REST que el backend debe implementar para que el modulo de **Formularios Clinicos** del frontend funcione correctamente. El frontend ya esta construido y consume estos endpoints a traves del proxy `/api/proxy/form-builder/...` que redirige a `/api/v1/...` en el backend.

---

## 1. Modelo de Base de Datos

### 1.1 Diagrama de Relaciones

```
┌───────────────────┐
│    fb_attribute    │
├───────────────────┤
│ id (PK)           │
│ code (UNIQUE)     │
│ label             │
│ input_type        │
│ is_required       │
│ placeholder       │
│ default_value     │
│ tooltip           │
│ validation_rules  │ ← JSON
│ options           │ ← JSON array
│ created_at        │
│ updated_at        │
└───────────────────┘

┌───────────────────┐       ┌─────────────────────┐
│   fb_template     │       │  fb_template_group   │
├───────────────────┤       ├─────────────────────┤
│ id (PK)           │──1:N──│ id (PK)             │
│ name              │       │ template_id (FK)    │
│ description       │       │ name                │
│ is_active         │       │ description         │
│ version           │       │ sort_order          │
│ created_at        │       │ icon                │
│ updated_at        │       │ is_collapsible      │
└───────────────────┘       └─────────────────────┘
                                      │
                                     1:N
                                      │
                            ┌─────────────────────────┐
                            │ fb_template_group_attr   │
                            ├─────────────────────────┤
                            │ id (PK)                  │
                            │ group_id (FK)            │
                            │ attribute_id (FK)        │
                            │ sort_order               │
                            │ is_required_override     │
                            │ label_override           │
                            │ width                    │
                            └─────────────────────────┘
```

### 1.2 Tabla `fb_attribute`

Almacena los campos individuales reutilizables.

```sql
CREATE TABLE fb_attribute (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    label           VARCHAR(255) NOT NULL,
    input_type      VARCHAR(30) NOT NULL,
    is_required     BOOLEAN NOT NULL DEFAULT FALSE,
    placeholder     VARCHAR(255),
    default_value   VARCHAR(500),
    tooltip         VARCHAR(500),
    validation_rules JSONB,          -- {"min_length":2,"max_length":100,"pattern":"^[a-z]+$","custom_message":"..."}
    options         JSONB,           -- [{"value":"opt1","label":"Opcion 1","sort_order":0}, ...]
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

**Valores validos para `input_type`:**
- `text`, `textarea`, `number`, `email`, `phone`, `date`, `datetime`
- `select`, `multiselect`, `radio`
- `checkbox`, `switch`, `file`

**Estructura de `validation_rules` (JSON):**
```json
{
  "min_length": 2,
  "max_length": 100,
  "min_value": 0,
  "max_value": 999,
  "pattern": "^[a-zA-Z]+$",
  "custom_message": "Solo se permiten letras"
}
```

**Estructura de `options` (JSON array):**
```json
[
  { "value": "opt_a", "label": "Opcion A", "sort_order": 0 },
  { "value": "opt_b", "label": "Opcion B", "sort_order": 1 }
]
```

### 1.3 Tabla `fb_template`

```sql
CREATE TABLE fb_template (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    version     INTEGER NOT NULL DEFAULT 1,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 1.4 Tabla `fb_template_group`

```sql
CREATE TABLE fb_template_group (
    id              BIGSERIAL PRIMARY KEY,
    template_id     BIGINT NOT NULL REFERENCES fb_template(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    icon            VARCHAR(50),
    is_collapsible  BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_tg_template ON fb_template_group(template_id);
```

### 1.5 Tabla `fb_template_group_attr`

```sql
CREATE TABLE fb_template_group_attr (
    id                   BIGSERIAL PRIMARY KEY,
    group_id             BIGINT NOT NULL REFERENCES fb_template_group(id) ON DELETE CASCADE,
    attribute_id         BIGINT NOT NULL REFERENCES fb_attribute(id) ON DELETE RESTRICT,
    sort_order           INTEGER NOT NULL DEFAULT 0,
    is_required_override BOOLEAN,
    label_override       VARCHAR(255),
    width                VARCHAR(10) NOT NULL DEFAULT 'full',

    UNIQUE (group_id, attribute_id)
);

CREATE INDEX idx_tga_group ON fb_template_group_attr(group_id);
CREATE INDEX idx_tga_attribute ON fb_template_group_attr(attribute_id);
```

**Valores validos para `width`:** `full`, `half`, `third`

---

## 2. Endpoints REST

Todos los endpoints estan bajo el prefijo `/api/v1/` y requieren autenticacion via header `Authorization: Bearer <token>` y `x-employee-id: <id>`.

### 2.1 Atributos

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/api/v1/form-builder/attributes` | Listar todos los atributos |
| `GET` | `/api/v1/form-builder/attributes/{id}` | Obtener un atributo por ID |
| `POST` | `/api/v1/form-builder/attributes` | Crear un nuevo atributo |
| `PUT` | `/api/v1/form-builder/attributes/{id}` | Actualizar un atributo |
| `DELETE` | `/api/v1/form-builder/attributes/{id}` | Eliminar un atributo |

#### GET /api/v1/form-builder/attributes

**Response 200:**
```json
[
  {
    "id": 1,
    "code": "nombre_paciente",
    "label": "Nombre del Paciente",
    "input_type": "text",
    "is_required": true,
    "placeholder": "Ingrese el nombre completo",
    "default_value": null,
    "tooltip": "Nombre legal del paciente",
    "validation_rules": {
      "min_length": 2,
      "max_length": 100
    },
    "options": null,
    "created_at": "2026-01-15T10:30:00Z",
    "updated_at": "2026-01-15T10:30:00Z"
  }
]
```

#### POST /api/v1/form-builder/attributes

**Request Body:**
```json
{
  "code": "tipo_sangre",
  "label": "Tipo de Sangre",
  "input_type": "select",
  "is_required": true,
  "placeholder": "Seleccione tipo de sangre",
  "default_value": null,
  "tooltip": null,
  "validation_rules": null,
  "options": [
    { "value": "a_pos", "label": "A+", "sort_order": 0 },
    { "value": "a_neg", "label": "A-", "sort_order": 1 },
    { "value": "b_pos", "label": "B+", "sort_order": 2 },
    { "value": "b_neg", "label": "B-", "sort_order": 3 },
    { "value": "o_pos", "label": "O+", "sort_order": 4 },
    { "value": "o_neg", "label": "O-", "sort_order": 5 },
    { "value": "ab_pos", "label": "AB+", "sort_order": 6 },
    { "value": "ab_neg", "label": "AB-", "sort_order": 7 }
  ]
}
```

**Response 201:** Retorna el objeto creado con `id`, `created_at`, `updated_at`.

#### PUT /api/v1/form-builder/attributes/{id}

**Request Body:** Mismo formato que POST (campos parciales aceptados).

**Response 200:** Retorna el objeto actualizado.

#### DELETE /api/v1/form-builder/attributes/{id}

**Response 204:** Sin contenido. Falla con 409 si el atributo esta en uso en algun grupo.

---

### 2.2 Plantillas (Templates)

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/api/v1/form-builder/templates` | Listar plantillas |
| `GET` | `/api/v1/form-builder/templates/{id}` | Obtener plantilla con grupos y atributos |
| `POST` | `/api/v1/form-builder/templates` | Crear plantilla |
| `PUT` | `/api/v1/form-builder/templates/{id}` | Actualizar plantilla |
| `DELETE` | `/api/v1/form-builder/templates/{id}` | Eliminar plantilla |

#### GET /api/v1/form-builder/templates

**Response 200:**
```json
[
  {
    "id": 1,
    "name": "Historia Clinica General",
    "description": "Plantilla estandar para consulta general",
    "is_active": true,
    "version": 1,
    "groups": [
      {
        "id": 1,
        "template_id": 1,
        "name": "Informacion Personal",
        "sort_order": 0,
        "is_collapsible": true,
        "attributes": [
          {
            "id": 1,
            "group_id": 1,
            "attribute_id": 1,
            "sort_order": 0,
            "is_required_override": null,
            "label_override": null,
            "width": "half",
            "attribute": {
              "id": 1,
              "code": "nombre_paciente",
              "label": "Nombre del Paciente",
              "input_type": "text",
              "is_required": true,
              "placeholder": "Ingrese el nombre",
              "options": null
            }
          }
        ]
      }
    ],
    "created_at": "2026-01-15T10:30:00Z",
    "updated_at": "2026-01-15T10:30:00Z"
  }
]
```

> **IMPORTANTE:** El endpoint GET de listado de plantillas DEBE incluir los `groups` y dentro de cada grupo los `attributes` con el objeto `attribute` embebido (JOIN). Esto permite al frontend mostrar contadores de campos y la vista previa sin requests adicionales.

#### GET /api/v1/form-builder/templates/{id}

**Response 200:** Misma estructura que un elemento del array anterior pero individual. DEBE incluir la relacion completa:
- `template.groups[]` ordenados por `sort_order`
- `groups[].attributes[]` ordenados por `sort_order`
- `attributes[].attribute` con el objeto `fb_attribute` completo

#### POST /api/v1/form-builder/templates

**Request Body:**
```json
{
  "name": "Historia Clinica Pediatrica",
  "description": "Formulario para consultas pediatricas",
  "is_active": true
}
```

**Response 201:** Retorna el objeto creado (sin grupos al inicio).

---

### 2.3 Grupos de Plantilla

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/api/v1/form-builder/templates/{templateId}/groups` | Listar grupos |
| `POST` | `/api/v1/form-builder/templates/{templateId}/groups` | Crear grupo |
| `PUT` | `/api/v1/form-builder/templates/{templateId}/groups/{groupId}` | Actualizar grupo |
| `DELETE` | `/api/v1/form-builder/templates/{templateId}/groups/{groupId}` | Eliminar grupo |
| `PUT` | `/api/v1/form-builder/templates/{templateId}/groups/reorder` | Reordenar grupos |

#### POST /api/v1/form-builder/templates/{templateId}/groups

**Request Body:**
```json
{
  "name": "Signos Vitales",
  "description": "Signos vitales al momento de la consulta",
  "sort_order": 1,
  "icon": "heart",
  "is_collapsible": true
}
```

**Response 201:** Retorna el grupo creado.

#### PUT /api/v1/form-builder/templates/{templateId}/groups/reorder

**Request Body:**
```json
{
  "ordered_ids": [3, 1, 2]
}
```

El backend debe actualizar el `sort_order` de cada grupo segun su posicion en el array.

**Response 200:** `{ "message": "ok" }`

---

### 2.4 Atributos de Grupo

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `POST` | `/api/v1/form-builder/templates/{tplId}/groups/{groupId}/attributes` | Agregar atributo |
| `PUT` | `/api/v1/form-builder/templates/{tplId}/groups/{groupId}/attributes/{gaId}` | Actualizar (orden, ancho, overrides) |
| `DELETE` | `/api/v1/form-builder/templates/{tplId}/groups/{groupId}/attributes/{gaId}` | Remover atributo del grupo |
| `PUT` | `/api/v1/form-builder/templates/{tplId}/groups/{groupId}/attributes/reorder` | Reordenar atributos |

#### POST .../groups/{groupId}/attributes

**Request Body:**
```json
{
  "attribute_id": 5,
  "sort_order": 2,
  "is_required_override": true,
  "label_override": "Presion Arterial Sistolica",
  "width": "half"
}
```

**Response 201:** Retorna el `fb_template_group_attr` creado CON el objeto `attribute` embebido:
```json
{
  "id": 10,
  "group_id": 1,
  "attribute_id": 5,
  "sort_order": 2,
  "is_required_override": true,
  "label_override": "Presion Arterial Sistolica",
  "width": "half",
  "attribute": {
    "id": 5,
    "code": "presion_arterial",
    "label": "Presion Arterial",
    "input_type": "number",
    "is_required": false,
    "placeholder": "mmHg",
    "options": null
  }
}
```

#### PUT .../groups/{groupId}/attributes/{gaId}

**Request Body (parcial):**
```json
{
  "width": "third",
  "is_required_override": false,
  "label_override": null,
  "sort_order": 3
}
```

#### PUT .../groups/{groupId}/attributes/reorder

**Request Body:**
```json
{
  "ordered_ids": [10, 8, 9]
}
```

---

## 3. Notas de Implementacion

### 3.1 Proxy del Frontend

El frontend envia requests a `/api/proxy/form-builder/...`. El proxy catch-all existente en `app/api/proxy/[...path]/route.ts` ya maneja esto automaticamente - **no se necesitan nuevas rutas proxy**. El proxy:

1. Recibe `/api/proxy/form-builder/attributes`
2. Elimina el prefijo conocido si es `auth`, `employees`, o `users`
3. Como `form-builder` no esta en esa lista, la ruta completa se usa: `form-builder/attributes`
4. Redirige a `{API_HOST}/api/v1/form-builder/attributes`

### 3.2 Autenticacion

Todos los endpoints requieren:
- Header `Authorization: Bearer <token>` 
- Header `x-employee-id: <id>`

El frontend ya envia estos headers automaticamente via `getAuthHeaders()`.

### 3.3 Manejo de Errores

El backend debe retornar errores en formato JSON:
```json
{
  "message": "Descripcion del error",
  "status": 400
}
```

Codigos de error esperados:
- `400` - Validacion fallida (campo faltante, codigo duplicado, etc.)
- `404` - Recurso no encontrado
- `409` - Conflicto (ej: eliminar atributo que esta en uso)
- `500` - Error interno

### 3.4 Validaciones del Backend

**Atributos:**
- `code` debe ser unico, solo letras, numeros y guion bajo
- `input_type` debe ser uno de los valores validos
- Si `input_type` es `select`, `multiselect` o `radio`, el campo `options` debe tener al menos 1 opcion
- Cada opcion debe tener `value` y `label` no vacios

**Templates:**
- `name` no puede estar vacio

**Grupos:**
- `name` no puede estar vacio
- `sort_order` >= 0

**Atributos de Grupo:**
- No se puede agregar el mismo `attribute_id` dos veces al mismo `group_id` (UNIQUE constraint)
- `width` debe ser `full`, `half` o `third`

### 3.5 Cascade Deletes

- Al eliminar un `fb_template`: se eliminan automaticamente sus `fb_template_group` y `fb_template_group_attr` (CASCADE)
- Al eliminar un `fb_template_group`: se eliminan automaticamente sus `fb_template_group_attr` (CASCADE)
- Al eliminar un `fb_attribute`: debe fallar si esta siendo usado en algun `fb_template_group_attr` (RESTRICT)

### 3.6 Orden de Implementacion Recomendado

1. Crear las 4 tablas con sus indices y constraints
2. Implementar CRUD de `fb_attribute` (mas simple, sin relaciones complejas)
3. Implementar CRUD de `fb_template` (incluir eager load de groups+attributes en GET)
4. Implementar CRUD de `fb_template_group`
5. Implementar CRUD de `fb_template_group_attr`
6. Implementar endpoints de reorder (groups y attributes)

### 3.7 Estructura de Paquetes Sugerida (Java/Spring Boot)

```
com.nexosalud.formbuilder/
├── controller/
│   ├── AttributeController.java
│   ├── TemplateController.java
│   ├── TemplateGroupController.java
│   └── TemplateGroupAttrController.java
├── service/
│   ├── AttributeService.java
│   ├── TemplateService.java
│   ├── TemplateGroupService.java
│   └── TemplateGroupAttrService.java
├── repository/
│   ├── AttributeRepository.java
│   ├── TemplateRepository.java
│   ├── TemplateGroupRepository.java
│   └── TemplateGroupAttrRepository.java
├── entity/
│   ├── Attribute.java
│   ├── Template.java
│   ├── TemplateGroup.java
│   └── TemplateGroupAttr.java
└── dto/
    ├── AttributeDTO.java
    ├── TemplateDTO.java
    ├── TemplateGroupDTO.java
    ├── TemplateGroupAttrDTO.java
    └── ReorderRequest.java
```

### 3.8 Ejemplo de Entity (JPA)

```java
@Entity
@Table(name = "fb_attribute")
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String code;
    
    @Column(nullable = false)
    private String label;
    
    @Column(name = "input_type", nullable = false, length = 30)
    private String inputType;
    
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;
    
    private String placeholder;
    
    @Column(name = "default_value", length = 500)
    private String defaultValue;
    
    @Column(length = 500)
    private String tooltip;
    
    @Type(JsonBinaryType.class)
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> options;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## 4. Resumen de URLs que el Frontend Consume

| Frontend URL | Backend URL | Metodos |
|---|---|---|
| `/api/proxy/form-builder/attributes` | `/api/v1/form-builder/attributes` | GET, POST |
| `/api/proxy/form-builder/attributes/{id}` | `/api/v1/form-builder/attributes/{id}` | GET, PUT, DELETE |
| `/api/proxy/form-builder/templates` | `/api/v1/form-builder/templates` | GET, POST |
| `/api/proxy/form-builder/templates/{id}` | `/api/v1/form-builder/templates/{id}` | GET, PUT, DELETE |
| `/api/proxy/form-builder/templates/{id}/groups` | `/api/v1/form-builder/templates/{id}/groups` | GET, POST |
| `/api/proxy/form-builder/templates/{id}/groups/{gId}` | `/api/v1/form-builder/templates/{id}/groups/{gId}` | PUT, DELETE |
| `/api/proxy/form-builder/templates/{id}/groups/reorder` | `/api/v1/form-builder/templates/{id}/groups/reorder` | PUT |
| `/api/proxy/form-builder/templates/{id}/groups/{gId}/attributes` | `/api/v1/form-builder/templates/{id}/groups/{gId}/attributes` | POST |
| `/api/proxy/form-builder/templates/{id}/groups/{gId}/attributes/{aId}` | `/api/v1/form-builder/templates/{id}/groups/{gId}/attributes/{aId}` | PUT, DELETE |
| `/api/proxy/form-builder/templates/{id}/groups/{gId}/attributes/reorder` | `/api/v1/form-builder/templates/{id}/groups/{gId}/attributes/reorder` | PUT |
