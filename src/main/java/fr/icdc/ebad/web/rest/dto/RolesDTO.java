package fr.icdc.ebad.web.rest.dto;

import lombok.Data;

/**
 * Created by dtrouillet on 07/03/2016.
 */

@Data
public class RolesDTO {
    private String loginUser;
    private boolean roleUser;
    private boolean roleAdmin;
}
