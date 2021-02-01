package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ToString
public class CreationSchedulingDto {
    @NotNull
    private Long batchId;
    @NotNull
    private Long environmentId;
    private String parameters;
    @NotBlank
    private String cron;
}
