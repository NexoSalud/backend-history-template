package com.reactive.nexo.formbuilder.dto;

public record GroupAttributeInput(
    Long attributeId, Integer sortOrder, Boolean isRequiredOverride,
    String labelOverride, String width, Long dependsOnAttrId, String dependsOnValue
) {}
