package fr.icdc.ebad.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Entite de normes permettant de rendre parametrable les differentes normes, chemins et interpretteur
 * de ligne de commande
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_norme")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Norme extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(unique = true, nullable = false)
    private String name;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(length = 255, nullable = false, name = "command_line")
    private String commandLine; //Powershell, sh, bash, zsh,...

    @NotNull
    @Size(min = 1, max = 255)
    @Column(nullable = false, name = "path_shell")
    private String pathShell;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(nullable = false, name = "ctrl_m_date")
    private String ctrlMDate;
}
