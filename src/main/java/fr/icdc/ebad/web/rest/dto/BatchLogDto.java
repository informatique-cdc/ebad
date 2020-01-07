package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

/**
 * Batch.
 */
@Data
public class BatchLogDto extends AbstractAuditingDto {
    private Long id;
    private String name;
    private String path;
    private String defaultParam;
    private String params;
}
