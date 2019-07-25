package fr.icdc.ebad.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Chaine.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false, exclude = "chaineAssociations")
@ToString(exclude = "chaineAssociations")
@Table(name = "t_chaine")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Chaine extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = false, nullable = false)
    private String name;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(length = 255, unique = false, nullable = false)
    private String description;

    @OneToMany(mappedBy = "chaine", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    @OrderBy(value = "batchOrder")
    private List<ChaineAssociation> chaineAssociations = new ArrayList<>();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "environnement_id")
    private Environnement environnement;
}
