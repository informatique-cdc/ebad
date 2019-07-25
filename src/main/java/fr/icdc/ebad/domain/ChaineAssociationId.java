package fr.icdc.ebad.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by dtrouillet on 13/06/2016.
 */
@Data
public class ChaineAssociationId implements Serializable {
    private static final long serialVersionUID = 1L;
    public long getBatch() {
        return batch;
    }

    public void setBatch(long batch) {
        this.batch = batch;
    }

    public long getChaine() {
        return chaine;
    }

    public void setChaine(long chaine) {
        this.chaine = chaine;
    }

    private int batchOrder;

    private long batch;

    private long chaine;
}
