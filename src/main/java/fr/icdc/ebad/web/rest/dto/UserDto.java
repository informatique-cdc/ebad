package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * A user.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserDto extends UserSimpleDto {
    private Set<AuthorityDto> authorities = new HashSet<>();
    private Set<UsageApplicationSimpleDto> usageApplications = new HashSet<>();
}
