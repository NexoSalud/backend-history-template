package com.reactive.nexo.formbuilder.controller;

import com.reactive.nexo.formbuilder.dto.ReorderRequest;
import com.reactive.nexo.formbuilder.dto.TemplateGroupAttrDTO;
import com.reactive.nexo.formbuilder.service.TemplateGroupAttrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/form-builder/templates/{templateId}/groups/{groupId}/attributes")
@RequiredArgsConstructor
@Tag(name = "Form Builder Group Attributes", description = "Endpoints for managing attributes within a group")
public class TemplateGroupAttrController {

    private final TemplateGroupAttrService templateGroupAttrService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an attribute to a group")
    public Mono<TemplateGroupAttrDTO> addAttribute(@PathVariable Long templateId, @PathVariable Long groupId,
            @RequestBody TemplateGroupAttrDTO dto) {
        return templateGroupAttrService.addAttributeToGroup(groupId, dto);
    }

    @PutMapping("/{attrId}")
    @Operation(summary = "Update an attribute in a group")
    public Mono<ResponseEntity<TemplateGroupAttrDTO>> updateAttribute(@PathVariable Long templateId,
            @PathVariable Long groupId, @PathVariable Long attrId, @RequestBody TemplateGroupAttrDTO dto) {
        return templateGroupAttrService.updateGroupAttribute(attrId, dto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{attrId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove an attribute from a group")
    public Mono<Void> removeAttribute(@PathVariable Long templateId, @PathVariable Long groupId,
            @PathVariable Long attrId) {
        return templateGroupAttrService.removeAttributeFromGroup(attrId);
    }

    @PutMapping("/reorder")
    @Operation(summary = "Reorder attributes in a group")
    public Mono<ResponseEntity<Void>> reorderAttributes(@PathVariable Long templateId, @PathVariable Long groupId,
            @RequestBody ReorderRequest request) {
        return templateGroupAttrService.reorderAttributes(request)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
}
