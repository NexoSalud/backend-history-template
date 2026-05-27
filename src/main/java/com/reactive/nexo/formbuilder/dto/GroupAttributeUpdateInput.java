package com.reactive.nexo.formbuilder.dto;

public record GroupAttributeUpdateInput(
    Integer sortOrder, Boolean isRequiredOverride,
    String labelOverride, String width, Long dependsOnAttrId, String dependsOnValue
) {}
