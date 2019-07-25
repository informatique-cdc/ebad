package fr.icdc.ebad.web.rest.dto;

import fr.icdc.ebad.domain.Directory;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by dtrouillet on 21/06/2016.
 */
public class FilesDto {
    private Directory directory;
    private String name;
    private Long size;
    private Date updateDate;
    private Date createDate;

    public FilesDto(Directory directory, String name, Long size, int updateMillis, int createMillis) {
        this.directory = directory;
        this.name = name;
        this.size = size;
        Calendar calendarUpdateTime = Calendar.getInstance();
        calendarUpdateTime.setTimeInMillis(updateMillis*1000L);
        this.updateDate =   calendarUpdateTime.getTime();
        Calendar calendarCreateTime = Calendar.getInstance();
        calendarCreateTime.setTimeInMillis(createMillis*1000L);
        this.createDate = calendarCreateTime.getTime();
    }

    public FilesDto() {
    }

    public Directory getDirectory() {
        return directory;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
