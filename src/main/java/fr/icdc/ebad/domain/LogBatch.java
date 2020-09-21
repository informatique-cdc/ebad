package fr.icdc.ebad.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by dtrouillet on 09/03/2016.
 */
@Entity
@Data
@Table(name = "t_log_batch")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class LogBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logbatch_generator")
    @SequenceGenerator(name = "logbatch_generator", sequenceName = "t_logbatch_id_seq")
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "environnement_id")
    private Environnement environnement;

    @NotNull
    @Column(unique = false, nullable = false, name = "log_date")
    private Date logDate;

    @NotNull
    @Column(unique = false, nullable = false, name = "execution_time")
    private Long executionTime;

    @NotNull
    @Column(unique = false, nullable = false, name = "return_code")
    private int returnCode;

    @Nullable
    @Column(unique = false, nullable = true)
    private String params;

    @NotNull
    @Column(unique = false, nullable = false, name = "date_traitement")
    private Date dateTraitement;


}
