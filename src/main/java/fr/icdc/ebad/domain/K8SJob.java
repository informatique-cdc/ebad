package fr.icdc.ebad.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Batch.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode(callSuper = false, exclude = {"chaineAssociations", "environnements"})
//@ToString(exclude = {"chaineAssociations", "environnements"})
@Table(name = "t_k8s_job")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class K8SJob extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(length = 50, nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "t_environnement_k8s_job",
            joinColumns = {@JoinColumn(name = "k8s_job_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "environnement_id", referencedColumnName = "id")})
    private Set<Environnement> environnements = new HashSet<>();

    @NotNull
    @Size(min = 1, max = 10000)
    @Column(nullable = false)
    private String manifest;

    @Nullable
    @Size(max = 255)
    @Column(name = "image")
    private String image;

    @Nullable
    @Size(max = 255)
    @Column(name = "args")
    private String args;

//    @OneToMany(mappedBy = "batch", orphanRemoval = true, cascade = CascadeType.ALL)
//    @JsonIgnore
//    private List<ChaineAssociation> chaineAssociations = new ArrayList<>();
//
//    @Transient
//    private String params;

}
