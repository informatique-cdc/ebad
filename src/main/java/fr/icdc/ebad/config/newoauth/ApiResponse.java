package fr.icdc.ebad.config.newoauth;

import lombok.Value;

@Value
public class ApiResponse {
    private Boolean success;
    private String message;
}