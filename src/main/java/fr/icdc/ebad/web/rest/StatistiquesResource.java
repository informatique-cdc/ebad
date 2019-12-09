package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.service.StatistiquesService;
import fr.icdc.ebad.web.rest.dto.StatistiquesDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by dtrouillet on 20/03/2018.
 */
@RestController
@RequestMapping("/statistics")
@Tag(name = "Statistics", description = "the statistic API")
public class StatistiquesResource {
    private final StatistiquesService statistiquesService;

    public StatistiquesResource(StatistiquesService statistiquesService) {
        this.statistiquesService = statistiquesService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StatistiquesDto> getGeneralStatistiques() {
        return ResponseEntity.ok(statistiquesService.generationStatistiques());
    }
}
