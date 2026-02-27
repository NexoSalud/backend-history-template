package com.reactive.nexo.formbuilder.controller;

import com.reactive.nexo.formbuilder.dto.AttributeDTO;
import com.reactive.nexo.formbuilder.service.AttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/form-builder/attributes")
@RequiredArgsConstructor
@Tag(name = "Form Builder Attributes", description = "Endpoints for managing attributes")
public class AttributeController {

    private final AttributeService attributeService;

    @GetMapping
    @Operation(summary = "List all attributes")
    public Flux<AttributeDTO> getAllAttributes() {
        return attributeService.getAllAttributes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get attribute by ID")
    public Mono<ResponseEntity<AttributeDTO>> getAttributeById(@PathVariable Long id) {
        return attributeService.getAttributeById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new attribute")
    public Mono<AttributeDTO> createAttribute(@RequestBody AttributeDTO dto) {
        return attributeService.createAttribute(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an attribute")
    public Mono<ResponseEntity<AttributeDTO>> updateAttribute(@PathVariable Long id, @RequestBody AttributeDTO dto) {
        return attributeService.updateAttribute(id, dto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an attribute")
    public Mono<ResponseEntity<java.util.Map<String, Boolean>>> deleteAttribute(@PathVariable Long id) {
        return attributeService.deleteAttribute(id)
                .thenReturn(ResponseEntity.ok(java.util.Collections.singletonMap("success", true)));
    }
}
