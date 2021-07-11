package fr.icdc.ebad.web.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by dtrouillet on 21/06/2016.
 */
@Data
@NoArgsConstructor
public class FilesDto {
    private DirectoryDto directory;
    private String name;
    private Long size;
    private LocalDateTime updateDate;
    private LocalDateTime createDate;
    private boolean isFolder;
    private String subDirectory;

    public FilesDto(DirectoryDto directory, String name, Long size, LocalDateTime updateDate, LocalDateTime createDate, boolean isFolder, String subDirectory) {
        this.directory = directory;
        this.name = name;
        this.size = size;
        this.updateDate = updateDate;
        this.createDate = createDate;
        this.isFolder = isFolder;
        this.subDirectory = subDirectory;
    }

}
