package com.reactive.nexo.formbuilder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateGroupDTO {
    private Long id;
    @JsonProperty("template_id")
    private Long templateId;
    private String name;
    private String description;
    @JsonProperty("sort_order")
    private Integer sortOrder;
    private String icon;
    @JsonProperty("is_collapsible")
    private Boolean isCollapsible;
    private List<TemplateGroupAttrDTO> attributes;
}
