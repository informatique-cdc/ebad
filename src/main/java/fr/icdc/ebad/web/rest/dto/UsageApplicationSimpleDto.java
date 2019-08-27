package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
public class UsageApplicationSimpleDto {
    private Long applicationId;
    private boolean canManage;
    private boolean canUse;
}
