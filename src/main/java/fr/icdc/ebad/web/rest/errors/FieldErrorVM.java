package fr.icdc.ebad.web.rest.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldErrorVM {
    private String objectName;
    private String field;
    private String message;
}
