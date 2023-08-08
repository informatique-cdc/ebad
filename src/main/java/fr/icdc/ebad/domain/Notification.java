package fr.icdc.ebad.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

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
    @CreatedDate
    @Column(nullable = false, name = "created_date")
    @Convert(converter= Jsr310JpaConverters.LocalDateConverter.class)
    private DateTime createdDate = DateTime.now();

    @NotNull
    @Column(nullable = false, name = "is_read")
    private boolean read = false;

    @NotNull
    @Column(nullable = false, name = "is_danger")
    private boolean danger = false;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "user_id")
    private User receiver;
}
