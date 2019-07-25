package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Chaine.
 */
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"environnement"})
@ToString(exclude = {"environnement"})
public class ChaineSimpleDto extends AbstractAuditingDto {
    private Long id;
    private String name;
    private String description;
    private BatchEnvironnementDto environnement;
}
