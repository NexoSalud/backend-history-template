package com.reactive.nexo.formbuilder.dto;

public record ValidationRuleInput(
    Integer minLength, Integer maxLength, Double minValue, Double maxValue,
    String pattern, String customMessage
) {}
