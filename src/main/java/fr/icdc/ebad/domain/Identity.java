package fr.icdc.ebad.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_identity")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Identity extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "identity_generator")
    @SequenceGenerator(name = "identity_generator", sequenceName = "t_identity_id_seq")
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String name;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String login;

    @Size(min = 1, max = 255)
    @Column
    private String password;

    @Size(min = 1, max = 2048)
    @Column
    private String privatekey;

    @Size(min = 1, max = 2048)
    @Column(name = "privatekey_path")
    private String privatekeyPath;

    @Size(min = 1, max = 255)
    @Column
    private String passphrase;

    @ManyToOne
    @JoinColumn(name = "available_application_id")
    @JsonBackReference
    private Application availableApplication;
}
