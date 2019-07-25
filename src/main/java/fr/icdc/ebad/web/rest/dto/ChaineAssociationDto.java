package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by dtrouillet on 13/06/2016.
 */
@Data
@EqualsAndHashCode(exclude = {"batch"})
@ToString(exclude = {"batch"})
public class ChaineAssociationDto {
    private int batchOrder;
    private ChaineAssociationBatchDto batch;
}
