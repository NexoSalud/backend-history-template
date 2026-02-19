package com.reactive.nexo.formbuilder.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("fb_attribute")
public class Attribute {

    @Id
    private Long id;

    @Column("code")
    private String code;

    @Column("label")
    private String label;

    @Column("input_type")
    private String inputType;

    @Column("is_required")
    private Boolean isRequired;

    @Column("placeholder")
    private String placeholder;

    @Column("default_value")
    private String defaultValue;

    @Column("tooltip")
    private String tooltip;

    @Column("validation_rules")
    private String validationRules; // Stored as JSON string directly for simplicity with R2DBC

    @Column("options")
    private String options; // Stored as JSON string

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
