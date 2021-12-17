package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

import java.util.Date;

@Data
public class LogBatchDto {
    private Long id;
    private String jobId;
    private BatchLogDto batch;
    private EnvironnementDto environnement;
    private Date logDate;
    private Long executionTime;
    private int returnCode;
    private String params;
    private Date dateTraitement;
    private String login;
    private String stdout;
    private String stderr;
}

