package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * View Model object for storing a user's credentials.
 */
@Data
public class LoginDto {

    @NotNull
    @Size(min = 1, max = 50)
    private String username;

    @NotNull
    @Size(min = ManagedUserDto.PASSWORD_MIN_LENGTH, max = ManagedUserDto.PASSWORD_MAX_LENGTH)
    private String password;

    private boolean rememberMe;
}
