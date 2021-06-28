package fr.icdc.ebad.web.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
