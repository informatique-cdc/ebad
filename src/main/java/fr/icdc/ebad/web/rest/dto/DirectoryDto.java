package fr.icdc.ebad.web.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Directory
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DirectoryDto extends AbstractAuditingDto {
    private Long id;
    private String name;
    private String path;
    private boolean canWrite;
    private boolean canExplore;
    private BatchEnvironnementDto environnement;
}
