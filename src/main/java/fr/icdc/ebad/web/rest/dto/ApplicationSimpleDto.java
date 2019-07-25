package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

@Data
public class ApplicationSimpleDto {
    private Long id;
    private String name;
    private String code;
    private String dateFichierPattern;
    private String dateParametrePattern;
}
