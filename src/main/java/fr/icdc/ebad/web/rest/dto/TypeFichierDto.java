package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TypeFichierDto extends AbstractAuditingDto {
    private Long id;
    private String name;
    private String pattern;
    private ApplicationSimpleDto application;
}
