package com.reactive.nexo.formbuilder.repository;

import com.reactive.nexo.formbuilder.entity.Attribute;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AttributeRepository extends R2dbcRepository<Attribute, Long> {

    @Query("SELECT COUNT(*) FROM fb_attribute")
    Mono<Long> countAttributes();

    @Query("SELECT * FROM fb_attribute ORDER BY id DESC LIMIT :limit OFFSET :offset")
    Flux<Attribute> findAllPaginated(int limit, int offset);
}
