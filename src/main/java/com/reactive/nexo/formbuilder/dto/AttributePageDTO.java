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
public class AttributePageDTO {
    private List<AttributeDTO> items;
    private long totalCount;
    private int page;
    private int perPage;
    private int totalPages;
}
