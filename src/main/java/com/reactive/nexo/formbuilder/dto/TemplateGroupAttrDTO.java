package com.reactive.nexo.formbuilder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateGroupAttrDTO {
    private Long id;
    @JsonProperty("group_id")
    private Long groupId;
    @JsonProperty("attribute_id")
    private Long attributeId;
    @JsonProperty("sort_order")
    private Integer sortOrder;
    @JsonProperty("is_required_override")
    private Boolean isRequiredOverride;
    @JsonProperty("label_override")
    private String labelOverride;
    private String width;
    private AttributeDTO attribute;
}
