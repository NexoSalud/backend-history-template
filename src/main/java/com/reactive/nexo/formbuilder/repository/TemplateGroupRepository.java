package com.reactive.nexo.formbuilder.repository;

import com.reactive.nexo.formbuilder.entity.TemplateGroup;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TemplateGroupRepository extends R2dbcRepository<TemplateGroup, Long> {
    Flux<TemplateGroup> findAllByTemplateIdOrderBySortOrderAsc(Long templateId);
}
