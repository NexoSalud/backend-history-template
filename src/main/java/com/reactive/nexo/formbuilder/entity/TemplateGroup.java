package com.reactive.nexo.formbuilder.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("fb_template_group")
public class TemplateGroup {

    @Id
    private Long id;

    @Column("template_id")
    private Long templateId;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("sort_order")
    private Integer sortOrder;

    @Column("icon")
    private String icon;

    @Column("is_collapsible")
    private Boolean isCollapsible;
}
