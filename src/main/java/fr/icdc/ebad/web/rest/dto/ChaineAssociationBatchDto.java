package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by dtrouillet on 13/06/2016.
 */
@Data
@EqualsAndHashCode
@ToString
public class ChaineAssociationBatchDto {
    private Long id;
    private String name;
}
