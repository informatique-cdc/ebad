package fr.icdc.ebad.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Created by dtrouillet on 13/06/2016.
 */
@Entity
@Data
@Table(name = "t_chaine_batch")
@IdClass(ChaineAssociationId.class)
public class ChaineAssociation {
    @Id
    @Column(name="batch_order")
    private int batchOrder;

    @Id
    @ManyToOne
    @JoinColumn(name = "chaine_id",referencedColumnName = "id")
    @JsonBackReference
    private Chaine chaine;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", referencedColumnName = "id")
    private Batch batch;
}
