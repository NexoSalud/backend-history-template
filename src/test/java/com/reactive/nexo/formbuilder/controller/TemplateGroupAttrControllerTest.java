package com.reactive.nexo.formbuilder.controller;

import com.reactive.nexo.formbuilder.dto.ReorderRequest;
import com.reactive.nexo.formbuilder.dto.TemplateGroupAttrDTO;
import com.reactive.nexo.formbuilder.service.TemplateGroupAttrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(TemplateGroupAttrController.class)
class TemplateGroupAttrControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TemplateGroupAttrService templateGroupAttrService;

    @Test
    void addAttribute() {
        TemplateGroupAttrDTO dto = TemplateGroupAttrDTO.builder().attributeId(1L).build();
        TemplateGroupAttrDTO createdDto = TemplateGroupAttrDTO.builder().id(1L).attributeId(1L).build();
        when(templateGroupAttrService.addAttributeToGroup(eq(1L), any(TemplateGroupAttrDTO.class))).thenReturn(Mono.just(createdDto));

        webTestClient.post().uri("/api/v1/form-builder/templates/1/groups/1/attributes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TemplateGroupAttrDTO.class)
                .consumeWith(response -> {
                    TemplateGroupAttrDTO responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getId().equals(1L);
                });
    }

    @Test
    void updateAttribute() {
        TemplateGroupAttrDTO dto = TemplateGroupAttrDTO.builder().width("half").build();
        when(templateGroupAttrService.updateGroupAttribute(eq(1L), any(TemplateGroupAttrDTO.class))).thenReturn(Mono.just(dto));

        webTestClient.put().uri("/api/v1/form-builder/templates/1/groups/1/attributes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TemplateGroupAttrDTO.class);
    }

    @Test
    void updateAttribute_notFound_returns404() {
        TemplateGroupAttrDTO dto = TemplateGroupAttrDTO.builder().width("half").build();
        when(templateGroupAttrService.updateGroupAttribute(eq(999L), any(TemplateGroupAttrDTO.class))).thenReturn(Mono.empty());

        webTestClient.put().uri("/api/v1/form-builder/templates/1/groups/1/attributes/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void removeAttribute() {
        when(templateGroupAttrService.removeAttributeFromGroup(1L, 1L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/form-builder/templates/1/groups/1/attributes/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void removeAttribute_nonExistent_stillReturnsSuccess() {
        when(templateGroupAttrService.removeAttributeFromGroup(1L, 999L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/form-builder/templates/1/groups/1/attributes/999")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void reorderAttributes() {
        ReorderRequest request = new ReorderRequest();
        request.setOrderedIds(Arrays.asList(1L, 2L, 3L));
        when(templateGroupAttrService.reorderAttributes(any(ReorderRequest.class))).thenReturn(Mono.empty());

        webTestClient.put().uri("/api/v1/form-builder/templates/1/groups/1/attributes/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void reorderAttributes_withEmptyList_stillSucceeds() {
        ReorderRequest request = new ReorderRequest();
        request.setOrderedIds(Collections.emptyList());
        when(templateGroupAttrService.reorderAttributes(any(ReorderRequest.class))).thenReturn(Mono.empty());

        webTestClient.put().uri("/api/v1/form-builder/templates/1/groups/1/attributes/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
}
