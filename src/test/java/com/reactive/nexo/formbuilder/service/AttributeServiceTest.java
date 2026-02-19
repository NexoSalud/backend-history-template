package com.reactive.nexo.formbuilder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactive.nexo.formbuilder.dto.AttributeDTO;
import com.reactive.nexo.formbuilder.entity.Attribute;
import com.reactive.nexo.formbuilder.repository.AttributeRepository;
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
    void createAttribute() {
        when(attributeRepository.save(any(Attribute.class))).thenReturn(Mono.just(attribute));

        StepVerifier.create(attributeService.createAttribute(attributeDTO))
                .expectNextMatches(dto -> dto.getId().equals(1L))
                .verifyComplete();
    }
}
