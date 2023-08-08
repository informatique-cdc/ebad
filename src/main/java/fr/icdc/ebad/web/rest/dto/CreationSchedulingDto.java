package fr.icdc.ebad.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
