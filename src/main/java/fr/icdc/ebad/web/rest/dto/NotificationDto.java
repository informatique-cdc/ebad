package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class NotificationDto {
    private Long id;
    private String content;
    private DateTime createdDate = DateTime.now();
    private boolean read = false;
}
