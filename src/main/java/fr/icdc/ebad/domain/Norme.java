package fr.icdc.ebad.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
