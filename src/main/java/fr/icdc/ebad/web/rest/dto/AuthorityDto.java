package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An authority (a security role) used by Spring Security.
 */
@Data
@EqualsAndHashCode
@ToString
public class AuthorityDto {
    private String name;
}
