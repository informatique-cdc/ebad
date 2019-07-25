package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * Batch.
 */
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"environnements"})
@ToString(exclude = {"environnements"})
public class BatchDto extends AbstractAuditingDto {
    private Long id;
    private String name;
    private Set<BatchEnvironnementDto> environnements = new HashSet<>();
    private String path;
    private String defaultParam;
    private String params;
}
