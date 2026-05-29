package com.reactive.nexo.formbuilder.service;

import com.reactive.nexo.formbuilder.dto.ReorderRequest;
import com.reactive.nexo.formbuilder.dto.TemplateGroupDTO;
import com.reactive.nexo.formbuilder.entity.TemplateGroup;
import com.reactive.nexo.formbuilder.repository.TemplateGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateGroupServiceTest {

    @Mock
    private TemplateGroupRepository templateGroupRepository;

    @InjectMocks
    private TemplateGroupService templateGroupService;

    private TemplateGroup group;
    private TemplateGroupDTO groupDTO;

    @BeforeEach
    void setUp() {
        group = TemplateGroup.builder()
                .id(1L)
                .templateId(1L)
                .name("Group 1")
                .description("Description")
                .sortOrder(0)
                .icon("folder")
                .isCollapsible(true)
                .build();

        groupDTO = TemplateGroupDTO.builder()
                .name("Group 1")
                .description("Description")
                .sortOrder(0)
                .icon("folder")
                .isCollapsible(true)
                .build();
    }

    @Test
    void getGroupsByTemplateId() {
        when(templateGroupRepository.findAllByTemplateIdOrderBySortOrderAsc(1L))
                .thenReturn(Flux.just(group));

        StepVerifier.create(templateGroupService.getGroupsByTemplateId(1L))
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                        dto.getName().equals("Group 1") &&
                        dto.getSortOrder() == 0 &&
                        dto.getIsCollapsible())
                .verifyComplete();
    }

    @Test
    void getGroupsByTemplateId_whenEmpty_returnsEmpty() {
        when(templateGroupRepository.findAllByTemplateIdOrderBySortOrderAsc(999L))
                .thenReturn(Flux.empty());

        StepVerifier.create(templateGroupService.getGroupsByTemplateId(999L))
                .verifyComplete();
    }

    @Test
    void createGroup() {
        when(templateGroupRepository.save(any(TemplateGroup.class))).thenReturn(Mono.just(group));

        StepVerifier.create(templateGroupService.createGroup(1L, groupDTO))
                .expectNextMatches(dto -> dto.getId().equals(1L) && dto.getTemplateId().equals(1L))
                .verifyComplete();
    }

    @Test
    void updateGroup() {
        TemplateGroup existing = TemplateGroup.builder()
                .id(1L)
                .templateId(1L)
                .name("Old Name")
                .sortOrder(0)
                .isCollapsible(true)
                .build();

        TemplateGroupDTO updateDto = TemplateGroupDTO.builder()
                .name("Updated Name")
                .description("Updated desc")
                .sortOrder(1)
                .icon("new-icon")
                .isCollapsible(false)
                .build();

        when(templateGroupRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(templateGroupRepository.save(any(TemplateGroup.class))).thenAnswer(invocation -> {
            TemplateGroup saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });

        StepVerifier.create(templateGroupService.updateGroup(1L, updateDto))
                .expectNextMatches(dto ->
                        dto.getName().equals("Updated Name") &&
                        dto.getSortOrder() == 1 &&
                        !dto.getIsCollapsible() &&
                        dto.getDescription().equals("Updated desc")
                )
                .verifyComplete();
    }

    @Test
    void updateGroup_notFound_returnsEmpty() {
        when(templateGroupRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(templateGroupService.updateGroup(999L, groupDTO))
                .verifyComplete();
    }

    @Test
    void deleteGroup() {
        when(templateGroupRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(templateGroupService.deleteGroup(1L))
                .verifyComplete();
    }

    @Test
    void reorderGroups() {
        TemplateGroup g1 = TemplateGroup.builder().id(1L).templateId(1L).name("G1").sortOrder(5).build();
        TemplateGroup g2 = TemplateGroup.builder().id(2L).templateId(1L).name("G2").sortOrder(10).build();

        ReorderRequest request = new ReorderRequest();
        request.setOrderedIds(Arrays.asList(1L, 2L));

        when(templateGroupRepository.findById(1L)).thenReturn(Mono.just(g1));
        when(templateGroupRepository.findById(2L)).thenReturn(Mono.just(g2));
        when(templateGroupRepository.save(any(TemplateGroup.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(templateGroupService.reorderGroups(request))
                .verifyComplete();
    }

    @Test
    void reorderGroups_withEmptyList_doesNothing() {
        ReorderRequest request = new ReorderRequest();
        request.setOrderedIds(Collections.emptyList());

        StepVerifier.create(templateGroupService.reorderGroups(request))
                .verifyComplete();
    }
}
