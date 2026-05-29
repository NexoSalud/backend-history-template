package com.reactive.nexo.formbuilder.controller;

import com.reactive.nexo.formbuilder.dto.ReorderRequest;
import com.reactive.nexo.formbuilder.dto.TemplateGroupDTO;
import com.reactive.nexo.formbuilder.service.TemplateGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(TemplateGroupController.class)
class TemplateGroupControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TemplateGroupService templateGroupService;

    @Test
    void getGroups() {
        TemplateGroupDTO dto = TemplateGroupDTO.builder().id(1L).name("Test Group").build();
        when(templateGroupService.getGroupsByTemplateId(1L)).thenReturn(Flux.just(dto));

        webTestClient.get().uri("/api/v1/form-builder/templates/1/groups")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TemplateGroupDTO.class)
                .hasSize(1);
    }

    @Test
    void getGroups_whenEmpty_returnsEmptyList() {
        when(templateGroupService.getGroupsByTemplateId(999L)).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/v1/form-builder/templates/999/groups")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TemplateGroupDTO.class)
                .hasSize(0);
    }

    @Test
    void createGroup() {
        TemplateGroupDTO dto = TemplateGroupDTO.builder().name("New Group").build();
        TemplateGroupDTO createdDto = TemplateGroupDTO.builder().id(1L).name("New Group").build();
        when(templateGroupService.createGroup(eq(1L), any(TemplateGroupDTO.class))).thenReturn(Mono.just(createdDto));

        webTestClient.post().uri("/api/v1/form-builder/templates/1/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TemplateGroupDTO.class)
                .consumeWith(response -> {
                    TemplateGroupDTO responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getId().equals(1L);
                    assert responseBody.getName().equals("New Group");
                });
    }

    @Test
    void updateGroup() {
        TemplateGroupDTO dto = TemplateGroupDTO.builder().name("Updated Group").build();
        when(templateGroupService.updateGroup(eq(1L), any(TemplateGroupDTO.class))).thenReturn(Mono.just(dto));

        webTestClient.put().uri("/api/v1/form-builder/templates/1/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TemplateGroupDTO.class)
                .consumeWith(response -> {
                    TemplateGroupDTO responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getName().equals("Updated Group");
                });
    }

    @Test
    void updateGroup_notFound_returns404() {
        TemplateGroupDTO dto = TemplateGroupDTO.builder().name("Updated Group").build();
        when(templateGroupService.updateGroup(eq(999L), any(TemplateGroupDTO.class))).thenReturn(Mono.empty());

        webTestClient.put().uri("/api/v1/form-builder/templates/1/groups/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteGroup() {
        when(templateGroupService.deleteGroup(1L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/form-builder/templates/1/groups/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void deleteGroup_nonExistent_stillReturnsSuccess() {
        when(templateGroupService.deleteGroup(999L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/form-builder/templates/1/groups/999")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void reorderGroups() {
        ReorderRequest request = new ReorderRequest();
        request.setOrderedIds(Arrays.asList(1L, 2L, 3L));
        when(templateGroupService.reorderGroups(any(ReorderRequest.class))).thenReturn(Mono.empty());

        webTestClient.put().uri("/api/v1/form-builder/templates/1/groups/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void reorderGroups_withEmptyList_stillSucceeds() {
        ReorderRequest request = new ReorderRequest();
        request.setOrderedIds(Collections.emptyList());
        when(templateGroupService.reorderGroups(any(ReorderRequest.class))).thenReturn(Mono.empty());

        webTestClient.put().uri("/api/v1/form-builder/templates/1/groups/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
}
