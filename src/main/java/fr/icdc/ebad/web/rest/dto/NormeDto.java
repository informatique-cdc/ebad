package fr.icdc.ebad.web.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class NormeDto extends ApplicationDto {
    private Long id;
    @NotBlank
    private String name;
    private String commandLine;
    private String pathShell;
    private String ctrlMDate;
}
