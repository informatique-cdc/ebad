package fr.icdc.ebad.web.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by dtrouillet on 03/03/2016.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvironnementInfoDTO {

    private Long id;
    private String diskSpace;
    private Date dateTraitement;

}
