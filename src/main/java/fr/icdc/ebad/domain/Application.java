package fr.icdc.ebad.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Application.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"environnements", "usageApplications"})
@ToString(exclude = {"environnements", "usageApplications"})
@Table(name = "t_application")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Application extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String name;

    @NotNull
    @Size(min = 3, max = 3)
    @Column(length = 3)
    private String code;

    @Size(max = 20)
    @Column(length = 20,name = "date_fichier_pattern")
    private String dateFichierPattern;

    @Size(max = 20)
    @Column(length = 20, name = "date_parametre_pattern")
    private String dateParametrePattern;

    @OneToMany(mappedBy = "application")
    @JsonManagedReference
    @OrderBy(value = "name")
    private Set<Environnement> environnements = new HashSet<>();

    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private Set<UsageApplication> usageApplications = new HashSet<>();

}
