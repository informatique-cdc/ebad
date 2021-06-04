package fr.icdc.ebad.web.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CurrentJobDto {
    private List<Long> batchs;
}
