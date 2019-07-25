package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Chaine.
 */
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"chaineAssociations", "environnement"})
@ToString(exclude = {"chaineAssociations", "environnement"})
public class ChaineDto extends AbstractAuditingDto {
    private Long id;
    private String name;
    private String description;
    private List<ChaineAssociationDto> chaineAssociations = new ArrayList<>();
    private BatchEnvironnementDto environnement;
}
