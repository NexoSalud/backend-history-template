package com.reactive.nexo.formbuilder.repository;

import com.reactive.nexo.formbuilder.entity.Template;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends R2dbcRepository<Template, Long> {
}
