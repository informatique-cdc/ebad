package fr.icdc.ebad.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
