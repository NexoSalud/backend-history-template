package com.reactive.nexo.formbuilder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReorderRequest {
    @JsonProperty("ordered_ids")
    private List<Long> orderedIds;
}
