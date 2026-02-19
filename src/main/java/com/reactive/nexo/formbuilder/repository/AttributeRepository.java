package com.reactive.nexo.formbuilder.repository;

import com.reactive.nexo.formbuilder.entity.Attribute;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeRepository extends R2dbcRepository<Attribute, Long> {
}
