package fr.icdc.ebad.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PublicIdentityDto {
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String login;
}
