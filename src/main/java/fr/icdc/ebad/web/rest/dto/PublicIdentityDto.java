package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PublicIdentityDto {
    private Long id;

    @NotNull
    private String login;
}
