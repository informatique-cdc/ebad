package fr.icdc.ebad.service.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BatchCsv {
    @CsvBindByName(required = true, column = "name")
    private String name;
    @CsvBindByName(required = true, column = "environnements")
    private String environnements;
    @CsvBindByName(required = true, column = "path")
    private String path;
    @CsvBindByName(column = "defaultParam")
    private String defaultParam;
}
