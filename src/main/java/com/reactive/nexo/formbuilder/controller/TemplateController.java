package com.reactive.nexo.formbuilder.controller;

import com.reactive.nexo.formbuilder.dto.TemplateDTO;
import com.reactive.nexo.formbuilder.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/form-builder/templates")
@RequiredArgsConstructor
@Tag(name = "Form Builder Templates", description = "Endpoints for managing templates")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    @Operation(summary = "List all templates with groups and attributes")
    public Flux<TemplateDTO> getAllTemplates() {
        return templateService.getAllTemplates();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID with groups and attributes")
    public Mono<ResponseEntity<TemplateDTO>> getTemplateById(@PathVariable Long id) {
        return templateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new template")
    public Mono<TemplateDTO> createTemplate(@RequestBody TemplateDTO dto) {
        return templateService.createTemplate(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a template")
    public Mono<ResponseEntity<TemplateDTO>> updateTemplate(@PathVariable Long id, @RequestBody TemplateDTO dto) {
        return templateService.updateTemplate(id, dto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a template")
    public Mono<Void> deleteTemplate(@PathVariable Long id) {
        return templateService.deleteTemplate(id);
    }
}
