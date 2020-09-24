package fr.icdc.ebad.web.rest.dto;

import fr.icdc.ebad.domain.Directory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by dtrouillet on 21/06/2016.
 */
@Data
@NoArgsConstructor
public class FilesDto {
    private Directory directory;
    private String name;
    private Long size;
    private Date updateDate;
    private Date createDate;
    private boolean isFolder;
    private String subDirectory;

    public FilesDto(Directory directory, String name, Long size, int updateMillis, int createMillis, boolean isFolder, String subDirectory) {
        this.directory = directory;
        this.name = name;
        this.size = size;
        Calendar calendarUpdateTime = Calendar.getInstance();
        calendarUpdateTime.setTimeInMillis(updateMillis * 1000L);
        this.updateDate = calendarUpdateTime.getTime();
        Calendar calendarCreateTime = Calendar.getInstance();
        calendarCreateTime.setTimeInMillis(createMillis * 1000L);
        this.createDate = calendarCreateTime.getTime();
        this.isFolder = isFolder;
        this.subDirectory = subDirectory;
    }

}
