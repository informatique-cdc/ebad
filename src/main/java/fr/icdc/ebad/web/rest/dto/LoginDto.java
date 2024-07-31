package fr.icdc.ebad.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
