package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private String content;
    private LocalDateTime createdDate = LocalDateTime.now();
    private boolean read = false;
}
