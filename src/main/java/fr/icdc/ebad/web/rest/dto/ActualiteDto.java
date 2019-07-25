package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class ActualiteDto extends AbstractAuditingDto {
    private Long id;
    private String title;
    private String content;
    private boolean draft = true;
}
