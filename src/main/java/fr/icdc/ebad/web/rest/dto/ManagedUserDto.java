package fr.icdc.ebad.web.rest.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

/**
 * View Model extending the UserAccountDto, which is meant to be used in the user management UI.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ManagedUserDto extends UserAccountDto {

    public static final int PASSWORD_MIN_LENGTH = 4;

    public static final int PASSWORD_MAX_LENGTH = 100;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String password;

    public ManagedUserDto() {
        // Empty constructor needed for Jackson.
    }
}
