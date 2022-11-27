package fr.icdc.ebad.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ResponseAccreditationRequestDto {
    @NotNull
    private Long id;
    @NotNull
    private boolean accepted;
}
