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
@Table("fb_template_group_attr")
public class TemplateGroupAttr {

    @Id
    private Long id;

    @Column("group_id")
    private Long groupId;

    @Column("attribute_id")
    private Long attributeId;

    @Column("sort_order")
    private Integer sortOrder;

    @Column("is_required_override")
    private Boolean isRequiredOverride;

    @Column("label_override")
    private String labelOverride;

    @Column("width")
    private String width;

    @Column("depends_on_attr_id")
    private Long dependsOnAttrId;

    @Column("depends_on_value")
    private String dependsOnValue;
}
