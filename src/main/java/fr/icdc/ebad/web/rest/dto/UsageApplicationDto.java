package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(exclude = {"application", "user"})
@EqualsAndHashCode(exclude = {"application", "user"})
public class UsageApplicationDto {
    private UserPublicDto user;
    private ApplicationDto application;
    private boolean canManage;
    private boolean canUse;
}
