package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Batch.
 */
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"environnements"})
@ToString(exclude = {"environnements"})
public class K8SJobDto extends AbstractAuditingDto {
    private Long id;
    @Size(min = 1, max = 50)
    private String name;
    @NotEmpty
    private Set<BatchEnvironnementDto> environnements = new HashSet<>();
    @NotBlank
    private String manifest;
    private String image;
    private String args;
}
