package com.reactive.nexo.formbuilder.controller;

import com.reactive.nexo.formbuilder.dto.ReorderRequest;
import com.reactive.nexo.formbuilder.dto.TemplateGroupDTO;
import com.reactive.nexo.formbuilder.service.TemplateGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/form-builder/templates/{templateId}/groups")
@RequiredArgsConstructor
@Tag(name = "Form Builder Template Groups", description = "Endpoints for managing template groups")
public class TemplateGroupController {

    private final TemplateGroupService templateGroupService;

    @GetMapping
    @Operation(summary = "List groups for a template")
    public Flux<TemplateGroupDTO> getGroups(@PathVariable Long templateId) {
        return templateGroupService.getGroupsByTemplateId(templateId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new group in a template")
    public Mono<TemplateGroupDTO> createGroup(@PathVariable Long templateId, @RequestBody TemplateGroupDTO dto) {
        return templateGroupService.createGroup(templateId, dto);
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Update a group")
    public Mono<ResponseEntity<TemplateGroupDTO>> updateGroup(@PathVariable Long templateId, @PathVariable Long groupId,
            @RequestBody TemplateGroupDTO dto) {
        return templateGroupService.updateGroup(groupId, dto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete a group")
    public Mono<ResponseEntity<java.util.Map<String, Boolean>>> deleteGroup(@PathVariable Long templateId, @PathVariable Long groupId) {
        return templateGroupService.deleteGroup(groupId)
                .thenReturn(ResponseEntity.ok(java.util.Collections.singletonMap("success", true)));
    }

    @PutMapping("/reorder")
    @Operation(summary = "Reorder groups in a template")
    public Mono<ResponseEntity<Void>> reorderGroups(@PathVariable Long templateId,
            @RequestBody ReorderRequest request) {
        return templateGroupService.reorderGroups(request)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
}
