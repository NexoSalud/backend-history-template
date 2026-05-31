package com.reactive.nexo.formbuilder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactive.nexo.formbuilder.dto.AttributeDTO;
import com.reactive.nexo.formbuilder.entity.Attribute;
import com.reactive.nexo.formbuilder.repository.AttributeRepository;
import io.r2dbc.postgresql.codec.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttributeServiceTest {

    @Mock
    private AttributeRepository attributeRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AttributeService attributeService;

    private Attribute attribute;
    private AttributeDTO attributeDTO;

    @BeforeEach
    void setUp() {
        attribute = Attribute.builder()
                .id(1L)
                .code("test_code")
                .label("Test Label")
                .inputType("text")
                .isRequired(true)
                .build();

        attributeDTO = AttributeDTO.builder()
                .code("test_code")
                .label("Test Label")
                .inputType("text")
                .isRequired(true)
                .build();
    }

    @Test
    void getAllAttributes() {
        when(attributeRepository.findAll()).thenReturn(Flux.just(attribute));

        StepVerifier.create(attributeService.getAllAttributes())
                .expectNextMatches(dto -> dto.getId().equals(1L) && dto.getCode().equals("test_code"))
                .verifyComplete();
    }

    @Test
    void getAllAttributes_whenEmpty_returnsEmpty() {
        when(attributeRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(attributeService.getAllAttributes())
                .verifyComplete();
    }

    @Test
    void getAttributeById() {
        when(attributeRepository.findById(1L)).thenReturn(Mono.just(attribute));

        StepVerifier.create(attributeService.getAttributeById(1L))
                .expectNextMatches(dto -> dto.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getAttributeById_notFound_returnsEmpty() {
        when(attributeRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(attributeService.getAttributeById(999L))
                .verifyComplete();
    }

    @Test
    void updateAttribute() {
        Attribute existing = Attribute.builder()
                .id(1L)
                .code("old_code")
                .label("Old Label")
                .inputType("text")
                .isRequired(false)
                .build();

        AttributeDTO updateDto = AttributeDTO.builder()
                .code("new_code")
                .label("New Label")
                .inputType("number")
                .isRequired(true)
                .build();

        Attribute saved = Attribute.builder()
                .id(1L)
                .code("new_code")
                .label("New Label")
                .inputType("number")
                .isRequired(true)
                .build();

        when(attributeRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(attributeRepository.save(any(Attribute.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(attributeService.updateAttribute(1L, updateDto))
                .expectNextMatches(dto -> dto.getCode().equals("new_code") && dto.getLabel().equals("New Label"))
                .verifyComplete();
    }

    @Test
    void updateAttribute_notFound_returnsEmpty() {
        AttributeDTO updateDto = AttributeDTO.builder().code("new").build();
        when(attributeRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(attributeService.updateAttribute(999L, updateDto))
                .verifyComplete();
    }

    @Test
    void deleteAttribute() {
        when(attributeRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(attributeService.deleteAttribute(1L))
                .verifyComplete();
    }

    @Test
    void createAttributeWithOptions() throws Exception {
        // Use a real ObjectMapper for this test to verify JSON conversion
        ObjectMapper realMapper = new ObjectMapper();
        realMapper.findAndRegisterModules();
        AttributeService serviceWithRealMapper = new AttributeService(attributeRepository, realMapper);

        List<Map<String, Object>> options = List.of(
                Map.of("value", "hombre", "label", "Hombre", "sort_order", 0),
                Map.of("value", "mujer", "label", "Mujer", "sort_order", 1)
        );

        AttributeDTO dto = AttributeDTO.builder()
                .code("genero")
                .label("Genero")
                .inputType("select")
                .isRequired(true)
                .options(options)
                .build();

        Attribute expectedEntity = Attribute.builder()
                .code("genero")
                .label("Genero")
                .inputType("select")
                .isRequired(true)
                .options(Json.of(realMapper.writeValueAsString(options)))
                .build();

        when(attributeRepository.save(any(Attribute.class))).thenReturn(Mono.just(expectedEntity));

        StepVerifier.create(serviceWithRealMapper.createAttribute(dto))
                .expectNextMatches(savedDto -> {
                    return savedDto.getCode().equals("genero") &&
                            savedDto.getOptions() != null &&
                            savedDto.getOptions().size() == 2;
                })
                .verifyComplete();
    }

    @Test
    void createAttribute_withNullJson_doesNotThrow() throws Exception {
        ObjectMapper realMapper = new ObjectMapper();
        realMapper.findAndRegisterModules();
        AttributeService serviceWithRealMapper = new AttributeService(attributeRepository, realMapper);

        AttributeDTO dto = AttributeDTO.builder()
                .code("test")
                .label("Test")
                .inputType("text")
                .isRequired(false)
                .validationRules(null)
                .options(null)
                .build();

        Attribute expectedEntity = Attribute.builder()
                .code("test")
                .label("Test")
                .inputType("text")
                .isRequired(false)
                .validationRules(null)
                .options(null)
                .build();

        when(attributeRepository.save(any(Attribute.class))).thenReturn(Mono.just(expectedEntity));

        StepVerifier.create(serviceWithRealMapper.createAttribute(dto))
                .expectNextMatches(savedDto ->
                        savedDto.getCode().equals("test") &&
                        savedDto.getValidationRules() == null &&
                        savedDto.getOptions() == null
                )
                .verifyComplete();
    }

    @Test
    void createAttribute_withValidationRules() throws Exception {
        ObjectMapper realMapper = new ObjectMapper();
        realMapper.findAndRegisterModules();
        AttributeService serviceWithRealMapper = new AttributeService(attributeRepository, realMapper);

        Map<String, Object> rules = Map.of(
                "minLength", 3,
                "maxLength", 50,
                "pattern", "^[a-zA-Z]+$"
        );

        AttributeDTO dto = AttributeDTO.builder()
                .code("name")
                .label("Name")
                .inputType("text")
                .isRequired(true)
                .validationRules(rules)
                .build();

        Attribute expectedEntity = Attribute.builder()
                .code("name")
                .label("Name")
                .inputType("text")
                .isRequired(true)
                .validationRules(Json.of(realMapper.writeValueAsString(rules)))
                .build();

        when(attributeRepository.save(any(Attribute.class))).thenReturn(Mono.just(expectedEntity));

        StepVerifier.create(serviceWithRealMapper.createAttribute(dto))
                .expectNextMatches(savedDto ->
                        savedDto.getValidationRules() != null &&
                        savedDto.getValidationRules().get("minLength").equals(3)
                )
                .verifyComplete();
    }
}
