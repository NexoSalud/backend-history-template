package com.reactive.nexo.formbuilder.controller;

import com.reactive.nexo.formbuilder.dto.TemplateDTO;
import com.reactive.nexo.formbuilder.service.TemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(TemplateController.class)
class TemplateControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TemplateService templateService;

    @Test
    void getAllTemplates() {
        TemplateDTO dto = TemplateDTO.builder().id(1L).name("test").build();
        when(templateService.getAllTemplates()).thenReturn(Flux.just(dto));

        webTestClient.get().uri("/api/v1/form-builder/templates")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TemplateDTO.class)
                .hasSize(1);
    }

    @Test
    void getAllTemplates_whenEmpty_returnsEmptyList() {
        when(templateService.getAllTemplates()).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/v1/form-builder/templates")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TemplateDTO.class)
                .hasSize(0);
    }

    @Test
    void getTemplateById_found() {
        TemplateDTO dto = TemplateDTO.builder().id(1L).name("test").build();
        when(templateService.getTemplateById(1L)).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/api/v1/form-builder/templates/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(TemplateDTO.class)
                .consumeWith(response -> {
                    TemplateDTO body = response.getResponseBody();
                    assert body != null;
                    assert body.getId().equals(1L);
                });
    }

    @Test
    void getTemplateById_notFound_returns404() {
        when(templateService.getTemplateById(999L)).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/form-builder/templates/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createTemplate() {
        TemplateDTO dto = TemplateDTO.builder().name("test").build();
        TemplateDTO createdDto = TemplateDTO.builder().id(1L).name("test").build();
        when(templateService.createTemplate(any(TemplateDTO.class))).thenReturn(Mono.just(createdDto));

        webTestClient.post().uri("/api/v1/form-builder/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TemplateDTO.class)
                .consumeWith(response -> {
                    TemplateDTO responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getId().equals(1L);
                });
    }

    @Test
    void updateTemplate() {
        TemplateDTO dto = TemplateDTO.builder().name("updated").build();
        when(templateService.updateTemplate(eq(1L), any(TemplateDTO.class))).thenReturn(Mono.just(dto));

        webTestClient.put().uri("/api/v1/form-builder/templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TemplateDTO.class);
    }

    @Test
    void updateTemplate_notFound_returns404() {
        TemplateDTO dto = TemplateDTO.builder().name("updated").build();
        when(templateService.updateTemplate(eq(999L), any(TemplateDTO.class))).thenReturn(Mono.empty());

        webTestClient.put().uri("/api/v1/form-builder/templates/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteTemplate() {
        when(templateService.deleteTemplate(1L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/form-builder/templates/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void deleteTemplate_nonExistent_stillReturnsSuccess() {
        // deleteById is fire-and-forget with R2DBC — no error for non-existent IDs
        when(templateService.deleteTemplate(999L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/form-builder/templates/999")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }
}
