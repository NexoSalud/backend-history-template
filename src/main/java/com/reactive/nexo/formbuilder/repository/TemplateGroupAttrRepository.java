package com.reactive.nexo.formbuilder.repository;

import com.reactive.nexo.formbuilder.entity.TemplateGroupAttr;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TemplateGroupAttrRepository extends R2dbcRepository<TemplateGroupAttr, Long> {
    Flux<TemplateGroupAttr> findAllByGroupIdOrderBySortOrderAsc(Long groupId);
}
