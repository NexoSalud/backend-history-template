package com.reactive.nexo.formbuilder.service;

import com.reactive.nexo.formbuilder.dto.TemplateDTO;
import com.reactive.nexo.formbuilder.entity.Template;
import com.reactive.nexo.formbuilder.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateGroupService templateGroupService;
    private final TemplateGroupAttrService templateGroupAttrService;

    public Flux<TemplateDTO> getAllTemplates() {
        return templateRepository.findAll()
                .flatMap(this::populateTemplateDetails);
    }

    public Mono<TemplateDTO> getTemplateById(Long id) {
        return templateRepository.findById(id)
                .flatMap(this::populateTemplateDetails);
    }

    private Mono<TemplateDTO> populateTemplateDetails(Template template) {
        TemplateDTO dto = toDTO(template);
        return templateGroupService.getGroupsByTemplateId(template.getId())
                .flatMap(groupDTO -> templateGroupAttrService.getAttributesByGroupId(groupDTO.getId())
                        .collectList()
                        .map(attrs -> {
                            groupDTO.setAttributes(attrs);
                            return groupDTO;
                        }))
                .collectList()
                .map(groups -> {
                    dto.setGroups(groups);
                    return dto;
                });
    }

    public Mono<TemplateDTO> createTemplate(TemplateDTO dto) {
        return templateRepository.save(toEntity(dto))
                .map(this::toDTO);
    }

    public Mono<TemplateDTO> updateTemplate(Long id, TemplateDTO dto) {
        return templateRepository.findById(id)
                .flatMap(existing -> {
                    Template updated = toEntity(dto);
                    updated.setId(existing.getId());
                    updated.setCreatedAt(existing.getCreatedAt());
                    return templateRepository.save(updated);
                })
                .map(this::toDTO);
    }

    public Mono<Void> deleteTemplate(Long id) {
        return templateRepository.deleteById(id);
    }

    private TemplateDTO toDTO(Template entity) {
        return TemplateDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Template toEntity(TemplateDTO dto) {
        return Template.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .version(dto.getVersion() != null ? dto.getVersion() : 1)
                .build();
    }
}
