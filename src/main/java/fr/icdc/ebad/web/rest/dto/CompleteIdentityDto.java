package fr.icdc.ebad.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteIdentityDto extends AbstractAuditingDto {
    private Long id;

    @NotNull
    private String login;
    @NotNull
    private String name;
    private String password;
    private String privatekey;
    private String privatekeyPath;
    private String passphrase;

    private Long availableApplication;
}
