package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

@Data
public class ApiTokenWithKeyDto {
    private Long id;
    private String name;
    private String token;
}
