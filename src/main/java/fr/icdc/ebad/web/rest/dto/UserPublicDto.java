package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class UserPublicDto {
    private String login;
    private String firstName;
    private String lastName;
}
