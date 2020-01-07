package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by dtrouillet on 20/03/2018.
 */
@Data
public class StatisticsDto {
    private Long usersNbr;
    private Long batchsRunnedNbrs;
    private Long avgExecutionTime;
    private Long batchsNbrs;
    private Long applicationsNbr;
    private List<StatisticByDayDto> statisticsByDay;
}
