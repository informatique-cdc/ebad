package fr.icdc.ebad.web.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticByDayDto {
    private String date;
    private Long nbr;
    private Double executionTime;
}
