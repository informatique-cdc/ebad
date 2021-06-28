package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class AbstractAuditingDto {
//    @NotNull
    private String createdBy;
//    @NotNull
    private LocalDateTime createdDate = LocalDateTime.now();
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate = LocalDateTime.now();
}
