package com.reactive.nexo.formbuilder.service;

import com.reactive.nexo.formbuilder.dto.AttributeDTO;
import com.reactive.nexo.formbuilder.dto.ReorderRequest;
import com.reactive.nexo.formbuilder.dto.TemplateGroupAttrDTO;
import com.reactive.nexo.formbuilder.entity.TemplateGroupAttr;
import com.reactive.nexo.formbuilder.repository.TemplateGroupAttrRepository;
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
class TemplateGroupAttrServiceTest {

    @Mock
    private TemplateGroupAttrRepository templateGroupAttrRepository;

    @Mock
    private AttributeService attributeService;

    @InjectMocks
    private TemplateGroupAttrService templateGroupAttrService;

    private TemplateGroupAttr groupAttr;
    private TemplateGroupAttrDTO groupAttrDTO;
    private AttributeDTO attributeDTO;

    @BeforeEach
    void setUp() {
        groupAttr = TemplateGroupAttr.builder()
                .id(1L)
                .groupId(1L)
                .attributeId(5L)
                .sortOrder(0)
                .width("full")
                .build();

        groupAttrDTO = TemplateGroupAttrDTO.builder()
                .attributeId(5L)
                .sortOrder(0)
                .width("full")
                .build();

        attributeDTO = AttributeDTO.builder()
                .id(5L)
                .code("test_code")
                .label("Test Label")
                .inputType("text")
                .isRequired(true)
                .build();
    }

    @Test
    void getAttributesByGroupId() {
        when(templateGroupAttrRepository.findAllByGroupIdOrderBySortOrderAsc(1L))
                .thenReturn(Flux.just(groupAttr));
        when(attributeService.getAttributeById(5L)).thenReturn(Mono.just(attributeDTO));

        StepVerifier.create(templateGroupAttrService.getAttributesByGroupId(1L))
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                        dto.getAttributeId().equals(5L) &&
                        dto.getAttribute() != null &&
                        dto.getAttribute().getCode().equals("test_code")
                )
                .verifyComplete();
    }

    @Test
    void getAttributesByGroupId_whenAttributeNotFound_includesNullAttribute() {
        when(templateGroupAttrRepository.findAllByGroupIdOrderBySortOrderAsc(1L))
                .thenReturn(Flux.just(groupAttr));
        when(attributeService.getAttributeById(5L)).thenReturn(Mono.empty());

        StepVerifier.create(templateGroupAttrService.getAttributesByGroupId(1L))
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                        dto.getAttributeId().equals(5L) &&
                        dto.getAttribute() == null
                )
                .verifyComplete();
    }

    @Test
    void getAttributesByGroupId_whenEmpty_returnsEmpty() {
        when(templateGroupAttrRepository.findAllByGroupIdOrderBySortOrderAsc(999L))
                .thenReturn(Flux.empty());

        StepVerifier.create(templateGroupAttrService.getAttributesByGroupId(999L))
                .verifyComplete();
    }

    @Test
    void addAttributeToGroup() {
        when(templateGroupAttrRepository.save(any(TemplateGroupAttr.class))).thenReturn(Mono.just(groupAttr));
        when(attributeService.getAttributeById(5L)).thenReturn(Mono.just(attributeDTO));

        StepVerifier.create(templateGroupAttrService.addAttributeToGroup(1L, groupAttrDTO))
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                        dto.getGroupId().equals(1L) &&
                        dto.getAttribute() != null
                )
                .verifyComplete();
    }

    @Test
    void updateGroupAttribute() {
        TemplateGroupAttr existing = TemplateGroupAttr.builder()
                .id(1L)
                .groupId(1L)
                .attributeId(5L)
                .sortOrder(0)
                .width("full")
                .build();

        TemplateGroupAttrDTO updateDto = TemplateGroupAttrDTO.builder()
                .sortOrder(1)
                .width("half")
                .labelOverride("Custom Label")
                .isRequiredOverride(true)
                .build();

        when(templateGroupAttrRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(templateGroupAttrRepository.save(any(TemplateGroupAttr.class))).thenAnswer(invocation -> {
            TemplateGroupAttr saved = invocation.getArgument(0);
            saved.setId(1L);
            return Mono.just(saved);
        });
        when(attributeService.getAttributeById(5L)).thenReturn(Mono.just(attributeDTO));

        StepVerifier.create(templateGroupAttrService.updateGroupAttribute(1L, updateDto))
                .expectNextMatches(dto ->
                        dto.getWidth().equals("half") &&
                        dto.getSortOrder() == 1 &&
                        "Custom Label".equals(dto.getLabelOverride())
                )
                .verifyComplete();
    }

    @Test
    void updateGroupAttribute_notFound_returnsEmpty() {
        when(templateGroupAttrRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(templateGroupAttrService.updateGroupAttribute(999L, groupAttrDTO))
                .verifyComplete();
    }

    @Test
    void removeAttributeFromGroup() {
        when(templateGroupAttrRepository.deleteByGroupIdAndAttributeId(1L, 5L)).thenReturn(Mono.empty());

        StepVerifier.create(templateGroupAttrService.removeAttributeFromGroup(1L, 5L))
                .verifyComplete();
    }

    @Test
    void reorderAttributes() {
        TemplateGroupAttr a1 = TemplateGroupAttr.builder().id(1L).groupId(1L).attributeId(5L).sortOrder(3).build();
        TemplateGroupAttr a2 = TemplateGroupAttr.builder().id(2L).groupId(1L).attributeId(6L).sortOrder(7).build();

        ReorderRequest request = new ReorderRequest();
        request.setOrderedIds(Arrays.asList(1L, 2L));

        when(templateGroupAttrRepository.findById(1L)).thenReturn(Mono.just(a1));
        when(templateGroupAttrRepository.findById(2L)).thenReturn(Mono.just(a2));
        when(templateGroupAttrRepository.save(any(TemplateGroupAttr.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(templateGroupAttrService.reorderAttributes(request))
                .verifyComplete();
    }

    @Test
    void reorderAttributes_withEmptyList_doesNothing() {
        ReorderRequest request = new ReorderRequest();
        request.setOrderedIds(Collections.emptyList());

        StepVerifier.create(templateGroupAttrService.reorderAttributes(request))
                .verifyComplete();
    }
}
