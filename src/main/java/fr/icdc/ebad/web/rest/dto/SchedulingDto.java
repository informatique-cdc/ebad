package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SchedulingDto extends AbstractAuditingDto {
    private Long id;
    private BatchLogDto batch;
    private EnvironnementDto environnement;
    private String parameters;
    private String cron;
}
