package com.reactive.nexo.formbuilder.service;

import com.reactive.nexo.formbuilder.dto.ReorderRequest;
import com.reactive.nexo.formbuilder.dto.TemplateGroupDTO;
import com.reactive.nexo.formbuilder.entity.TemplateGroup;
import com.reactive.nexo.formbuilder.repository.TemplateGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateGroupService {

    private final TemplateGroupRepository templateGroupRepository;

    public Flux<TemplateGroupDTO> getGroupsByTemplateId(Long templateId) {
        return templateGroupRepository.findAllByTemplateIdOrderBySortOrderAsc(templateId)
                .map(this::toDTO);
    }

    public Mono<TemplateGroupDTO> createGroup(Long templateId, TemplateGroupDTO dto) {
        dto.setTemplateId(templateId);
        return templateGroupRepository.save(toEntity(dto))
                .map(this::toDTO);
    }

    public Mono<TemplateGroupDTO> updateGroup(Long groupId, TemplateGroupDTO dto) {
        return templateGroupRepository.findById(groupId)
                .flatMap(existing -> {
                    TemplateGroup updated = toEntity(dto);
                    updated.setId(existing.getId());
                    updated.setTemplateId(existing.getTemplateId());
                    return templateGroupRepository.save(updated);
                })
                .map(this::toDTO);
    }

    public Mono<Void> deleteGroup(Long groupId) {
        return templateGroupRepository.deleteById(groupId);
    }

    @Transactional
    public Mono<Void> reorderGroups(ReorderRequest request) {
        List<Long> orderedIds = request.getOrderedIds();
        return Flux.range(0, orderedIds.size())
                .concatMap(i -> {
                    Long id = orderedIds.get(i);
                    return templateGroupRepository.findById(id)
                            .flatMap(group -> {
                                group.setSortOrder(i);
                                return templateGroupRepository.save(group);
                            });
                })
                .then();
    }

    private TemplateGroupDTO toDTO(TemplateGroup entity) {
        return TemplateGroupDTO.builder()
                .id(entity.getId())
                .templateId(entity.getTemplateId())
                .name(entity.getName())
                .description(entity.getDescription())
                .sortOrder(entity.getSortOrder())
                .icon(entity.getIcon())
                .isCollapsible(entity.getIsCollapsible())
                .build();
    }

    private TemplateGroup toEntity(TemplateGroupDTO dto) {
        return TemplateGroup.builder()
                .templateId(dto.getTemplateId())
                .name(dto.getName())
                .description(dto.getDescription())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .icon(dto.getIcon())
                .isCollapsible(dto.getIsCollapsible() != null ? dto.getIsCollapsible() : true)
                .build();
    }
}
