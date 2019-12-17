package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.service.GlobalSettingService;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/global-settings")
public class GlobalSettingResource {
    private final GlobalSettingService globalSettingService;

    public GlobalSettingResource(GlobalSettingService globalSettingService) {
        this.globalSettingService = globalSettingService;
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> getValue(@PathVariable String key) throws EbadServiceException {
        return ResponseEntity.ok(globalSettingService.getValue(key));
    }
}
