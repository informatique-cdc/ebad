package fr.icdc.ebad.web.rest.dto;

import java.util.Date;

/**
 * Created by dtrouillet on 03/03/2016.
 */
public class EnvironnementInfoDTO {

    private Long id;
    private String diskSpace;
    private Date dateTraitement;

    public EnvironnementInfoDTO(Long id, String diskSpace, Date dateTraitement) {
        this.id = id;
        this.diskSpace = diskSpace;
        this.dateTraitement = dateTraitement;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(String diskSpace) {
        this.diskSpace = diskSpace;
    }

    public Date getDateTraitement() {
        return dateTraitement;
    }

    public void setDateTraitement(Date dateTraitement) {
        this.dateTraitement = dateTraitement;
    }
}
