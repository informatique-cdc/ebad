package fr.icdc.ebad.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by dtrouillet on 13/06/2016.
 */
@Data
public class ChaineAssociationId implements Serializable {
//    @Serial
    private static final long serialVersionUID = 1L;

    private int batchOrder;

    private long batch;

    private long chaine;
}
