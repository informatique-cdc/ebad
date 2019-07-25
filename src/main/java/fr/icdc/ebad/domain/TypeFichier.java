package fr.icdc.ebad.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * TypeFichier
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "t_type_fichier")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TypeFichier extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(length = 255, unique = false, nullable = false)
    private String name;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(length = 255, unique = false, nullable = false)
    private String pattern;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;
}
