package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

@Data
public class JobStateDto {
    private String id;
    private String state;
    private LogBatchDto log;
}
