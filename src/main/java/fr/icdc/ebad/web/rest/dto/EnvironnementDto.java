package fr.icdc.ebad.web.rest.dto;


import fr.icdc.ebad.domain.AbstractAuditingEntity;
import lombok.*;

import java.util.Date;

/**
 * Environnement.
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EnvironnementDto extends AbstractAuditingEntity {
    private Long id;
    private String name;
    private String host;
    private PublicIdentityDto identity;
    private String homePath;
    private String prefix;
    private float diskSpace;
    private Date dateTraitement;
    private NormeDto norme;
}
