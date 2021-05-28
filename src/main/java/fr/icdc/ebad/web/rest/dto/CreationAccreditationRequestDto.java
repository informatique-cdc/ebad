package fr.icdc.ebad.web.rest.dto;

import fr.icdc.ebad.web.rest.validator.AccreditationValidator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@AccreditationValidator
public class CreationAccreditationRequestDto {
    @NotNull
    private boolean wantManage;

    @NotNull
    private boolean wantUse;

    @NotNull(message = "{error.validation.notnull.application}")
    private Long applicationId;
}
