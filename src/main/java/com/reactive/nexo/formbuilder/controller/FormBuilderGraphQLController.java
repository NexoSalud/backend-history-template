package com.reactive.nexo.formbuilder.controller;

import com.reactive.nexo.formbuilder.dto.*;
import com.reactive.nexo.formbuilder.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FormBuilderGraphQLController {

    private final TemplateService templateService;
    private final TemplateGroupService templateGroupService;
    private final AttributeService attributeService;
    private final TemplateGroupAttrService templateGroupAttrService;

    // ========================
    // QUERIES
    // ========================

    @QueryMapping
    public Flux<TemplateDTO> templates() {
        return templateService.getAllTemplates();
    }

    @QueryMapping
    public Mono<TemplateDTO> template(@Argument Long id) {
        return templateService.getTemplateById(id);
    }

    @QueryMapping
    public Flux<TemplateGroupDTO> groups(@Argument Long templateId) {
        return templateGroupService.getGroupsByTemplateId(templateId);
    }

    @QueryMapping
    public Flux<AttributeDTO> attributes() {
        return attributeService.getAllAttributes();
    }

    @QueryMapping
    public Mono<AttributeDTO> attribute(@Argument Long id) {
        return attributeService.getAttributeById(id);
    }

    @QueryMapping
    public Flux<TemplateGroupAttrDTO> groupAttributes(@Argument Long groupId) {
        return templateGroupAttrService.getAttributesByGroupId(groupId);
    }

    // ========================
    // FIELD RESOLVERS
    // ========================

    /**
     * Resuelve lazy el campo groups en Template.
     * Si ya está poblado (queries existentes vía populateTemplateDetails), lo retorna directo.
     * Si es null (createTemplate/updateTemplate), lo carga desde la DB.
     */
    @SchemaMapping(typeName = "Template", field = "groups")
    public Flux<TemplateGroupDTO> resolveGroups(TemplateDTO template) {
        List<TemplateGroupDTO> existing = template.getGroups();
        if (existing != null) {
            return Flux.fromIterable(existing);
        }
        return templateGroupService.getGroupsByTemplateId(template.getId());
    }

    /**
     * Resuelve lazy el campo attributes en TemplateGroup.
     * Si ya está poblado (vía populateTemplateDetails), lo retorna directo.
     * Si es null (createGroup/updateGroup), lo carga desde la DB.
     */
    @SchemaMapping(typeName = "TemplateGroup", field = "attributes")
    public Flux<TemplateGroupAttrDTO> resolveAttributes(TemplateGroupDTO group) {
        List<TemplateGroupAttrDTO> existing = group.getAttributes();
        if (existing != null) {
            return Flux.fromIterable(existing);
        }
        return templateGroupAttrService.getAttributesByGroupId(group.getId());
    }

    // ========================
    // MUTATIONS — Templates
    // ========================

    @MutationMapping
    public Mono<TemplateDTO> createTemplate(@Argument TemplateCreateInput input) {
        TemplateDTO dto = new TemplateDTO();
        dto.setName(input.name());
        dto.setDescription(input.description());
        dto.setIsActive(input.isActive() != null ? input.isActive() : true);
        dto.setVersion(1);
        return templateService.createTemplate(dto);
    }

    @MutationMapping
    public Mono<TemplateDTO> updateTemplate(@Argument Long id, @Argument TemplateUpdateInput input) {
        TemplateDTO dto = new TemplateDTO();
        dto.setName(input.name());
        dto.setDescription(input.description());
        dto.setIsActive(input.isActive());
        return templateService.updateTemplate(id, dto);
    }

    @MutationMapping
    public Mono<Boolean> deleteTemplate(@Argument Long id) {
        return templateService.deleteTemplate(id).then(Mono.just(true));
    }

    // ========================
    // MUTATIONS — Groups
    // ========================

    @MutationMapping
    public Mono<TemplateGroupDTO> createGroup(@Argument Long templateId, @Argument GroupCreateInput input) {
        TemplateGroupDTO dto = new TemplateGroupDTO();
        dto.setName(input.name());
        dto.setDescription(input.description());
        dto.setSortOrder(input.sortOrder());
        dto.setIcon(input.icon());
        dto.setIsCollapsible(input.isCollapsible() != null ? input.isCollapsible() : true);
        return templateGroupService.createGroup(templateId, dto);
    }

    @MutationMapping
    public Mono<TemplateGroupDTO> updateGroup(@Argument Long templateId, @Argument Long groupId, @Argument GroupUpdateInput input) {
        TemplateGroupDTO dto = new TemplateGroupDTO();
        dto.setName(input.name());
        dto.setDescription(input.description());
        dto.setSortOrder(input.sortOrder());
        dto.setIcon(input.icon());
        dto.setIsCollapsible(input.isCollapsible());
        return templateGroupService.updateGroup(groupId, dto);
    }

    @MutationMapping
    public Mono<Boolean> deleteGroup(@Argument Long templateId, @Argument Long groupId) {
        return templateGroupService.deleteGroup(groupId).then(Mono.just(true));
    }

    @MutationMapping
    public Mono<Boolean> reorderGroups(@Argument Long templateId, @Argument List<Long> orderedIds) {
        ReorderRequest req = new ReorderRequest();
        req.setOrderedIds(orderedIds);
        return templateGroupService.reorderGroups(req).then(Mono.just(true));
    }

    // ========================
    // MUTATIONS — Attributes (global)
    // ========================

    @MutationMapping
    public Mono<AttributeDTO> createAttribute(@Argument AttributeCreateInput input) {
        AttributeDTO dto = new AttributeDTO();
        dto.setCode(input.code());
        dto.setLabel(input.label());
        dto.setInputType(input.inputType());
        dto.setIsRequired(input.isRequired() != null ? input.isRequired() : false);
        dto.setPlaceholder(input.placeholder());
        dto.setDefaultValue(input.defaultValue());
        dto.setTooltip(input.tooltip());
        return attributeService.createAttribute(dto);
    }

    @MutationMapping
    public Mono<AttributeDTO> updateAttribute(@Argument Long id, @Argument AttributeUpdateInput input) {
        AttributeDTO dto = new AttributeDTO();
        dto.setCode(input.code());
        dto.setLabel(input.label());
        if (input.inputType() != null) dto.setInputType(input.inputType());
        dto.setIsRequired(input.isRequired());
        dto.setPlaceholder(input.placeholder());
        dto.setDefaultValue(input.defaultValue());
        dto.setTooltip(input.tooltip());
        return attributeService.updateAttribute(id, dto);
    }

    @MutationMapping
    public Mono<Boolean> deleteAttribute(@Argument Long id) {
        return attributeService.deleteAttribute(id).then(Mono.just(true));
    }

    // ========================
    // MUTATIONS — Group Attributes
    // ========================

    @MutationMapping
    public Mono<TemplateGroupAttrDTO> addAttributeToGroup(@Argument Long templateId, @Argument Long groupId,
                                                          @Argument GroupAttributeInput input) {
        TemplateGroupAttrDTO dto = new TemplateGroupAttrDTO();
        dto.setAttributeId(input.attributeId());
        dto.setSortOrder(input.sortOrder());
        dto.setIsRequiredOverride(input.isRequiredOverride());
        dto.setLabelOverride(input.labelOverride());
        dto.setWidth(input.width() != null ? input.width() : "full");
        dto.setDependsOnAttrId(input.dependsOnAttrId());
        dto.setDependsOnValue(input.dependsOnValue());
        return templateGroupAttrService.addAttributeToGroup(groupId, dto);
    }

    @MutationMapping
    public Mono<TemplateGroupAttrDTO> updateGroupAttribute(@Argument Long templateId, @Argument Long groupId,
                                                           @Argument Long attrId, @Argument GroupAttributeUpdateInput input) {
        TemplateGroupAttrDTO dto = new TemplateGroupAttrDTO();
        dto.setSortOrder(input.sortOrder());
        dto.setIsRequiredOverride(input.isRequiredOverride());
        dto.setLabelOverride(input.labelOverride());
        if (input.width() != null) dto.setWidth(input.width());
        dto.setDependsOnAttrId(input.dependsOnAttrId());
        dto.setDependsOnValue(input.dependsOnValue());
        return templateGroupAttrService.updateGroupAttribute(attrId, dto);
    }

    @MutationMapping
    public Mono<Boolean> removeAttributeFromGroup(@Argument Long templateId, @Argument Long groupId, @Argument Long attrId) {
        return templateGroupAttrService.removeAttributeFromGroup(groupId, attrId).then(Mono.just(true));
    }

    @MutationMapping
    public Mono<Boolean> reorderGroupAttributes(@Argument Long templateId, @Argument Long groupId, @Argument List<Long> orderedIds) {
        ReorderRequest req = new ReorderRequest();
        req.setOrderedIds(orderedIds);
        return templateGroupAttrService.reorderAttributes(req).then(Mono.just(true));
    }
}
