package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CompleteIdentityDto {
    private Long id;

    @NotNull
    private String login;
    private String name;
    private String password;
    private String privatekey;
    private String privatekeyPath;
    private String passphrase;
}
