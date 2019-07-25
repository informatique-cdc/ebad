package fr.icdc.ebad.web.rest.dto;


import com.fasterxml.jackson.annotation.JsonBackReference;
import fr.icdc.ebad.domain.AbstractAuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Environnement.
 */
@EqualsAndHashCode(callSuper = false, exclude = "application")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "application")
public class EnvironnementCreationDto extends AbstractAuditingEntity {
    private String name;
    @JsonBackReference
    private ApplicationDto application;
    private String host;
    private String login;
    private String homePath;
    private String prefix;
    private NormeDto norme;
}
