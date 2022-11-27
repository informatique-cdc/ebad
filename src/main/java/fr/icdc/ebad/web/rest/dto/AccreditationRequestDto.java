package fr.icdc.ebad.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AccreditationRequestDto extends AbstractAuditingDto {
    private Long id;
    private UserSimpleDto user;

    @NotNull
    private boolean wantManage;

    @NotNull
    private boolean wantUse;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ApplicationSimpleDto application;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String state;
}
