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
    void getAttributeById() {
        when(attributeRepository.findById(1L)).thenReturn(Mono.just(attribute));

        StepVerifier.create(attributeService.getAttributeById(1L))
                .expectNextMatches(dto -> dto.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void createAttributeWithOptions() throws Exception {
        // Use a real ObjectMapper for this test to verify JSON conversion
        ObjectMapper realMapper = new ObjectMapper();
        realMapper.findAndRegisterModules();
        AttributeService serviceWithRealMapper = new AttributeService(attributeRepository, realMapper);

        java.util.List<java.util.Map<String, Object>> options = java.util.List.of(
                java.util.Map.of("value", "hombre", "label", "Hombre", "sort_order", 0),
                java.util.Map.of("value", "mujer", "label", "Mujer", "sort_order", 1)
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
}
