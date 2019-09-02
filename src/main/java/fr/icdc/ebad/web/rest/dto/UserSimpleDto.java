package fr.icdc.ebad.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * A user.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserSimpleDto extends AbstractAuditingDto {
    private Long id;
    private String login;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private boolean activated = false;
    private String langKey;
    private String activationKey;

    @JsonSerialize
    @JsonDeserialize
    private String token;

}
