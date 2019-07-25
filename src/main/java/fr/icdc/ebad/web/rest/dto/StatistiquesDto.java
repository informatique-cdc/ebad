package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by dtrouillet on 20/03/2018.
 */
@Data
public class StatistiquesDto {
    private Long nbrUtilisateurs;
    private Long nbrBatchs;
    private Long nbrApplications;
    private Long nbrBatchsLances;
    private Long tpsMoyenBatch;
    private List<Long> nbrBatchLancesParJour;
}
