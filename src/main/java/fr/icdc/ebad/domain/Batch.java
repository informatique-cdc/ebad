package fr.icdc.ebad.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Batch.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"chaineAssociations", "environnements"})
@ToString(exclude = {"chaineAssociations", "environnements"})
@Table(name = "t_batch")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Batch extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(length = 50, nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "t_environnement_batch",
            joinColumns = {@JoinColumn(name = "batch_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "environnement_id", referencedColumnName = "id")})
    private Set<Environnement> environnements = new HashSet<>();

    @NotNull
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String path;

    @Nullable
    @Size(max = 255)
    @Column(name = "default_param")
    private String defaultParam;

    @OneToMany(mappedBy = "batch", orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ChaineAssociation> chaineAssociations = new ArrayList<>();

    @Transient
    private String params;

}
