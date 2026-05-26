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

    public Mono<TemplateGroupAttrDTO> addAttributeToGroup(Long groupId, TemplateGroupAttrDTO dto) {
        dto.setGroupId(groupId);
        return templateGroupAttrRepository.save(toEntity(dto))
                .flatMap(this::populateAttribute);
    }

    public Mono<TemplateGroupAttrDTO> updateGroupAttribute(Long id, TemplateGroupAttrDTO dto) {
        return templateGroupAttrRepository.findById(id)
                .flatMap(existing -> {
                    TemplateGroupAttr updated = toEntity(dto);
                    updated.setId(existing.getId());
                    updated.setGroupId(existing.getGroupId());
                    updated.setAttributeId(existing.getAttributeId());
                    return templateGroupAttrRepository.save(updated);
                })
                .flatMap(this::populateAttribute);
    }

    public Mono<Void> removeAttributeFromGroup(Long groupId, Long attributeId) {
        return templateGroupAttrRepository.deleteById(attributeId);
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
