package fr.icdc.ebad.domain;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;

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
 * Entite des notifications permettant d'enregistrer des notications
 */
@Data
@Entity
@Table(name = "t_notification")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Size(min = 1, max = 10000)
    @Column(length = 10000, nullable = false)
    private String content;

    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @CreatedDate
    @Column(nullable = false, name = "created_date")
    private DateTime createdDate = DateTime.now();

    @NotNull
    @Column(nullable = false, name = "is_read")
    private boolean read = false;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "user_id")
    private User receiver;
}
