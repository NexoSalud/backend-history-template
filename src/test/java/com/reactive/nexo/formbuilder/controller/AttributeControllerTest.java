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

import static org.mockito.ArgumentMatchers.any;
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
}
