package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false, exclude = {"environnements", "users"})
@ToString(exclude = {"environnements", "users"})
public class ApplicationDto extends AbstractAuditingDto {
    private Long id;
    private String name;
    private String code;
    private String dateFichierPattern;
    private String dateParametrePattern;
    private Set<EnvironnementDto> environnements = new HashSet<>();
    private Set<UserAccountDto> users = new HashSet<>();
}
