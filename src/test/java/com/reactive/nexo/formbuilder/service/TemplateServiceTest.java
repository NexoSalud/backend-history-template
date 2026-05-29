package com.reactive.nexo.formbuilder.service;

import com.reactive.nexo.formbuilder.dto.TemplateDTO;
import com.reactive.nexo.formbuilder.dto.TemplateGroupAttrDTO;
import com.reactive.nexo.formbuilder.dto.TemplateGroupDTO;
import com.reactive.nexo.formbuilder.entity.Template;
import com.reactive.nexo.formbuilder.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateGroupService templateGroupService;

    @Mock
    private TemplateGroupAttrService templateGroupAttrService;

    @InjectMocks
    private TemplateService templateService;

    private Template template;
    private TemplateDTO templateDTO;

    @BeforeEach
    void setUp() {
        template = Template.builder()
                .id(1L)
                .name("Test Template")
                .description("Description")
                .isActive(true)
                .version(1)
                .build();

        templateDTO = TemplateDTO.builder()
                .name("Test Template")
                .description("Description")
                .isActive(true)
                .version(1)
                .build();
    }

    @Test
    void getAllTemplates() {
        when(templateRepository.findAll()).thenReturn(Flux.just(template));
        when(templateGroupService.getGroupsByTemplateId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(templateService.getAllTemplates())
                .expectNextMatches(dto -> dto.getId().equals(1L) && dto.getName().equals("Test Template"))
                .verifyComplete();
    }

    @Test
    void getAllTemplates_whenEmpty_returnsEmpty() {
        when(templateRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(templateService.getAllTemplates())
                .verifyComplete();
    }

    @Test
    void getTemplateById() {
        when(templateRepository.findById(1L)).thenReturn(Mono.just(template));
        when(templateGroupService.getGroupsByTemplateId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(templateService.getTemplateById(1L))
                .expectNextMatches(dto -> dto.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getTemplateById_notFound_returnsEmpty() {
        when(templateRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(templateService.getTemplateById(999L))
                .verifyComplete();
    }

    @Test
    void createTemplate() {
        when(templateRepository.save(any(Template.class))).thenReturn(Mono.just(template));

        StepVerifier.create(templateService.createTemplate(templateDTO))
                .expectNextMatches(dto -> dto.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getTemplateById_withGroupsAndAttributes_populatesNestedData() {
        TemplateGroupDTO groupDTO = TemplateGroupDTO.builder().id(10L).name("Group 1").build();

        when(templateRepository.findById(1L)).thenReturn(Mono.just(template));
        when(templateGroupService.getGroupsByTemplateId(1L)).thenReturn(Flux.just(groupDTO));
        when(templateGroupAttrService.getAttributesByGroupId(10L)).thenReturn(Flux.empty());

        StepVerifier.create(templateService.getTemplateById(1L))
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                        dto.getGroups() != null &&
                        dto.getGroups().size() == 1 &&
                        dto.getGroups().get(0).getName().equals("Group 1") &&
                        dto.getGroups().get(0).getAttributes() != null &&
                        dto.getGroups().get(0).getAttributes().isEmpty()
                )
                .verifyComplete();
    }

    @Test
    void getTemplateById_withGroupHavingAttributes_populatesAll() {
        TemplateGroupDTO groupDTO = TemplateGroupDTO.builder().id(10L).name("Group 1").build();
        TemplateGroupAttrDTO attrDTO = TemplateGroupAttrDTO.builder().id(100L).attributeId(5L).width("full").build();

        when(templateRepository.findById(1L)).thenReturn(Mono.just(template));
        when(templateGroupService.getGroupsByTemplateId(1L)).thenReturn(Flux.just(groupDTO));
        when(templateGroupAttrService.getAttributesByGroupId(10L)).thenReturn(Flux.just(attrDTO));

        StepVerifier.create(templateService.getTemplateById(1L))
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                        dto.getGroups().size() == 1 &&
                        dto.getGroups().get(0).getAttributes().size() == 1 &&
                        dto.getGroups().get(0).getAttributes().get(0).getAttributeId().equals(5L)
                )
                .verifyComplete();
    }

    @Test
    void updateTemplate() {
        when(templateRepository.findById(1L)).thenReturn(Mono.just(template));
        when(templateRepository.save(any(Template.class))).thenReturn(Mono.just(template));

        StepVerifier.create(templateService.updateTemplate(1L, templateDTO))
                .expectNextMatches(dto -> dto.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void updateTemplate_notFound_returnsEmpty() {
        when(templateRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(templateService.updateTemplate(999L, templateDTO))
                .verifyComplete();
    }

    @Test
    void deleteTemplate() {
        when(templateRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(templateService.deleteTemplate(1L))
                .verifyComplete();
    }
}
