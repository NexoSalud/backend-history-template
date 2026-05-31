package com.reactive.nexo.formbuilder.dto;

import java.util.List;

public record AttributeUpdateInput(
    String code, String label, String inputType, Boolean isRequired,
    String placeholder, String defaultValue, String tooltip,
    List<AttributeOptionInput> options, ValidationRuleInput validationRules
) {}
