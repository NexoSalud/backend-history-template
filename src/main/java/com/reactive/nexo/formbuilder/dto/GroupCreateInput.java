package com.reactive.nexo.formbuilder.dto;

public record GroupCreateInput(String name, String description, Integer sortOrder, String icon, Boolean isCollapsible) {}
