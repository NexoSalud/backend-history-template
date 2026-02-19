package com.reactive.nexo.formbuilder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactive.nexo.formbuilder.dto.AttributeDTO;
import com.reactive.nexo.formbuilder.entity.Attribute;
import com.reactive.nexo.formbuilder.repository.AttributeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttributeService {

    private final AttributeRepository attributeRepository;
    private final ObjectMapper objectMapper;

    public Flux<AttributeDTO> getAllAttributes() {
        return attributeRepository.findAll()
                .map(this::toDTO);
    }

    public Mono<AttributeDTO> getAttributeById(Long id) {
        return attributeRepository.findById(id)
                .map(this::toDTO);
    }

    public Mono<AttributeDTO> createAttribute(AttributeDTO dto) {
        return attributeRepository.save(toEntity(dto))
                .map(this::toDTO);
    }

    public Mono<AttributeDTO> updateAttribute(Long id, AttributeDTO dto) {
        return attributeRepository.findById(id)
                .flatMap(existing -> {
                    Attribute updated = toEntity(dto);
                    updated.setId(existing.getId());
                    updated.setCreatedAt(existing.getCreatedAt());
                    return attributeRepository.save(updated);
                })
                .map(this::toDTO);
    }

    public Mono<Void> deleteAttribute(Long id) {
        return attributeRepository.deleteById(id);
    }

    private AttributeDTO toDTO(Attribute entity) {
        return AttributeDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .label(entity.getLabel())
                .inputType(entity.getInputType())
                .isRequired(entity.getIsRequired())
                .placeholder(entity.getPlaceholder())
                .defaultValue(entity.getDefaultValue())
                .tooltip(entity.getTooltip())
                .validationRules(parseJsonMap(entity.getValidationRules()))
                .options(parseJsonList(entity.getOptions()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Attribute toEntity(AttributeDTO dto) {
        return Attribute.builder()
                .code(dto.getCode())
                .label(dto.getLabel())
                .inputType(dto.getInputType())
                .isRequired(dto.getIsRequired() != null ? dto.getIsRequired() : false)
                .placeholder(dto.getPlaceholder())
                .defaultValue(dto.getDefaultValue())
                .tooltip(dto.getTooltip())
                .validationRules(toJson(dto.getValidationRules()))
                .options(toJson(dto.getOptions()))
                .build();
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON map", e);
            return null;
        }
    }

    private List<Map<String, Object>> parseJsonList(String json) {
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON list", e);
            return null;
        }
    }

    private String toJson(Object obj) {
        if (obj == null)
            return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting to JSON", e);
            return null;
        }
    }
}
