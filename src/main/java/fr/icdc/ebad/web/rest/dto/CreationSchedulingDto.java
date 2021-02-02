package fr.icdc.ebad.web.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreationSchedulingDto {
    @NotNull
    private Long batchId;
    @NotNull
    private Long environmentId;
    private String parameters;
    @NotBlank
    private String cron;
}
