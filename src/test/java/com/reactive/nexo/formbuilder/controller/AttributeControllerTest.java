package com.reactive.nexo.formbuilder.controller;

import com.reactive.nexo.formbuilder.dto.AttributeDTO;
import com.reactive.nexo.formbuilder.service.AttributeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(AttributeController.class)
class AttributeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AttributeService attributeService;

    @Test
    void getAllAttributes() {
        AttributeDTO dto = AttributeDTO.builder().id(1L).code("test").build();
        when(attributeService.getAllAttributes()).thenReturn(Flux.just(dto));

        webTestClient.get().uri("/api/v1/form-builder/attributes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AttributeDTO.class)
                .hasSize(1);
    }

    @Test
    void getAllAttributes_whenEmpty_returnsEmptyList() {
        when(attributeService.getAllAttributes()).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/v1/form-builder/attributes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AttributeDTO.class)
                .hasSize(0);
    }

    @Test
    void getAttributeById_found() {
        AttributeDTO dto = AttributeDTO.builder().id(1L).code("test").build();
        when(attributeService.getAttributeById(1L)).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/api/v1/form-builder/attributes/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AttributeDTO.class)
                .consumeWith(response -> {
                    AttributeDTO body = response.getResponseBody();
                    assert body != null;
                    assert body.getId().equals(1L);
                });
    }

    @Test
    void getAttributeById_notFound_returns404() {
        when(attributeService.getAttributeById(999L)).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/form-builder/attributes/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createAttribute() {
        AttributeDTO dto = AttributeDTO.builder().code("test").build();
        AttributeDTO createdDto = AttributeDTO.builder().id(1L).code("test").build();
        when(attributeService.createAttribute(any(AttributeDTO.class))).thenReturn(Mono.just(createdDto));

        webTestClient.post().uri("/api/v1/form-builder/attributes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AttributeDTO.class)
                .consumeWith(response -> {
                    AttributeDTO responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getId().equals(1L);
                });
    }

    @Test
    void updateAttribute() {
        AttributeDTO dto = AttributeDTO.builder().code("updated").build();
        when(attributeService.updateAttribute(eq(1L), any(AttributeDTO.class))).thenReturn(Mono.just(dto));

        webTestClient.put().uri("/api/v1/form-builder/attributes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AttributeDTO.class);
    }

    @Test
    void updateAttribute_notFound_returns404() {
        AttributeDTO dto = AttributeDTO.builder().code("updated").build();
        when(attributeService.updateAttribute(eq(999L), any(AttributeDTO.class))).thenReturn(Mono.empty());

        webTestClient.put().uri("/api/v1/form-builder/attributes/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteAttribute() {
        when(attributeService.deleteAttribute(1L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/form-builder/attributes/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void deleteAttribute_nonExistent_stillReturnsSuccess() {
        when(attributeService.deleteAttribute(999L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/form-builder/attributes/999")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }
}
