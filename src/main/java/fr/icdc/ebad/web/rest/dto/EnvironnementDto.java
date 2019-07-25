package fr.icdc.ebad.web.rest.dto;


import fr.icdc.ebad.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    private String login;
    private String homePath;
    private String prefix;
    private float diskSpace;
    private Date dateTraitement;
    //    private Set<BatchDto> batchs;
//    private Set<LogBatchDto> logBatchs;
    private NormeDto norme;
}
