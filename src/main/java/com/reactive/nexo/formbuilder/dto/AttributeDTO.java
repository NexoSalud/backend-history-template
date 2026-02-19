package com.reactive.nexo.formbuilder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDTO {
    private Long id;
    private String code;
    private String label;
    @JsonProperty("input_type")
    private String inputType;
    @JsonProperty("is_required")
    private Boolean isRequired;
    private String placeholder;
    @JsonProperty("default_value")
    private String defaultValue;
    private String tooltip;
    @JsonProperty("validation_rules")
    private Map<String, Object> validationRules;
    private List<Map<String, Object>> options;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
