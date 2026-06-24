package com.reactive.nexo.formbuilder.service;

import com.reactive.nexo.formbuilder.dto.AttributeDTO;
import com.reactive.nexo.formbuilder.dto.ReorderRequest;
import com.reactive.nexo.formbuilder.dto.TemplateGroupAttrDTO;
import com.reactive.nexo.formbuilder.entity.TemplateGroupAttr;
import com.reactive.nexo.formbuilder.repository.TemplateGroupAttrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateGroupAttrService {

    private final TemplateGroupAttrRepository templateGroupAttrRepository;
    private final AttributeService attributeService;

    public Flux<TemplateGroupAttrDTO> getAttributesByGroupId(Long groupId) {
        return templateGroupAttrRepository.findAllByGroupIdOrderBySortOrderAsc(groupId)
                .flatMap(this::populateAttribute);
    }

    private Mono<TemplateGroupAttrDTO> populateAttribute(TemplateGroupAttr entity) {
        TemplateGroupAttrDTO dto = toDTO(entity);
        return attributeService.getAttributeById(entity.getAttributeId())
                .map(attrDTO -> {
                    dto.setAttribute(attrDTO);
                    return dto;
                })
                .defaultIfEmpty(dto);
    }

    /**
     * Valida que dependsOnAttrId, si no es null, referencie un fb_attribute existente.
     */
    private Mono<Long> validateDependsOnAttr(Long dependsOnAttrId) {
        if (dependsOnAttrId == null) return Mono.just(0L);
        return attributeService.getAttributeById(dependsOnAttrId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "depends_on_attr_id " + dependsOnAttrId
                                + " no existe en fb_attribute")))
                .map(attr -> dependsOnAttrId);
    }

    public Mono<TemplateGroupAttrDTO> addAttributeToGroup(Long groupId, TemplateGroupAttrDTO dto) {
        dto.setGroupId(groupId);
        return validateDependsOnAttr(dto.getDependsOnAttrId())
                .flatMap(v -> templateGroupAttrRepository.save(toEntity(dto)))
                .flatMap(this::populateAttribute);
    }

    public Mono<TemplateGroupAttrDTO> updateGroupAttribute(Long id, TemplateGroupAttrDTO dto) {
        return templateGroupAttrRepository.findById(id)
                .flatMap(existing -> {
                    TemplateGroupAttr updated = toEntity(dto);
                    updated.setId(existing.getId());
                    updated.setGroupId(existing.getGroupId());
                    updated.setAttributeId(existing.getAttributeId());
                    // Preservar valores existentes si el DTO no los trae
                    if (dto.getDependsOnAttrId() == null) {
                        updated.setDependsOnAttrId(existing.getDependsOnAttrId());
                    }
                    if (dto.getDependsOnValue() == null) {
                        updated.setDependsOnValue(existing.getDependsOnValue());
                    }
                    if (dto.getSortOrder() == null) {
                        updated.setSortOrder(existing.getSortOrder());
                    }
                    if (dto.getIsRequiredOverride() == null) {
                        updated.setIsRequiredOverride(existing.getIsRequiredOverride());
                    }
                    if (dto.getLabelOverride() == null) {
                        updated.setLabelOverride(existing.getLabelOverride());
                    }
                    if (dto.getWidth() == null) {
                        updated.setWidth(existing.getWidth());
                    }
                    return validateDependsOnAttr(updated.getDependsOnAttrId())
                            .flatMap(v -> templateGroupAttrRepository.save(updated));
                })
                .flatMap(this::populateAttribute);
    }

    public Mono<Void> removeAttributeFromGroup(Long groupId, Long attributeId) {
        return templateGroupAttrRepository.deleteByGroupIdAndAttributeId(groupId, attributeId);
    }

    @Transactional
    public Mono<Void> reorderAttributes(ReorderRequest request) {
        List<Long> orderedIds = request.getOrderedIds();
        return Flux.range(0, orderedIds.size())
                .concatMap(i -> {
                    Long id = orderedIds.get(i);
                    return templateGroupAttrRepository.findById(id)
                            .flatMap(attr -> {
                                attr.setSortOrder(i);
                                return templateGroupAttrRepository.save(attr);
                            });
                })
                .then();
    }

    private TemplateGroupAttrDTO toDTO(TemplateGroupAttr entity) {
        return TemplateGroupAttrDTO.builder()
                .id(entity.getId())
                .groupId(entity.getGroupId())
                .attributeId(entity.getAttributeId())
                .sortOrder(entity.getSortOrder())
                .isRequiredOverride(entity.getIsRequiredOverride())
                .labelOverride(entity.getLabelOverride())
                .width(entity.getWidth())
                .dependsOnAttrId(entity.getDependsOnAttrId())
                .dependsOnValue(entity.getDependsOnValue())
                .build();
    }

    private TemplateGroupAttr toEntity(TemplateGroupAttrDTO dto) {
        return TemplateGroupAttr.builder()
                .groupId(dto.getGroupId())
                .attributeId(dto.getAttributeId())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .isRequiredOverride(dto.getIsRequiredOverride())
                .labelOverride(dto.getLabelOverride())
                .width(dto.getWidth() != null ? dto.getWidth() : "full")
                .dependsOnAttrId(dto.getDependsOnAttrId())
                .dependsOnValue(dto.getDependsOnValue())
                .build();
    }
}
