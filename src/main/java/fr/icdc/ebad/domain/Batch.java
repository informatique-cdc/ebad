package fr.icdc.ebad.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Batch.
 */
@Entity
@Data
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
