package fr.icdc.ebad.domain;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Environnement.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"batchs", "logBatchs"})
@ToString(exclude = {"batchs", "logBatchs"})
@Table(name = "t_environnement")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Environnement extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "plugin_id")
    private String pluginId;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = false, nullable = false)
    private String name;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "application_id")
    @JsonBackReference
    private Application application;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(length = 255, nullable = false)
    private String host;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(length = 255, nullable = false)
    private String login;

    @Nullable
    @Size(min = 1, max = 255)
    @Column(length = 255, nullable = true, name = "home_path")
    private String homePath;

    @NotNull
    @Size(max = 5)
    @Column(length = 5, nullable = false)
    @ColumnDefault("''")
    private String prefix;

    @Builder.Default
    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "t_environnement_batch",
            joinColumns = {@JoinColumn(name = "environnement_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "batch_id", referencedColumnName = "id")})
    private Set<Batch> batchs = new HashSet<>();

    @Builder.Default
    @OneToMany(orphanRemoval = true, mappedBy = "environnement")
    @Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE })
    @Fetch(FetchMode.SELECT)
    @Column
    @JsonIgnore
    private Set<LogBatch> logBatchs = new HashSet<>();

    @Transient
    private float diskSpace;

    @Transient
    private Date dateTraitement;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "norme_id")
    private Norme norme;
}
