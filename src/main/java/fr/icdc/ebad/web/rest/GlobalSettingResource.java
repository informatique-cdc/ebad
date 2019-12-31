package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.GlobalSetting;
import fr.icdc.ebad.service.GlobalSettingService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.GlobalSettingKeyValueDto;
import fr.icdc.ebad.web.rest.dto.GlobalSettingValueDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/global-settings")
@Tag(name = "Global Setting", description = "the global setting request API")
public class GlobalSettingResource {
    private final GlobalSettingService globalSettingService;
    private final MapperFacade mapper;

    public GlobalSettingResource(GlobalSettingService globalSettingService, MapperFacade mapper) {
        this.globalSettingService = globalSettingService;
        this.mapper = mapper;
    }

    @GetMapping("/{key}")
    public ResponseEntity<GlobalSettingKeyValueDto> getValue(@PathVariable String key) throws EbadServiceException {
        GlobalSetting globalSetting = globalSettingService.getValue(key);
        return ResponseEntity.ok(mapper.map(globalSetting, GlobalSettingKeyValueDto.class));
    }

    @PostMapping("/{key}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<GlobalSettingKeyValueDto> setValue(@PathVariable String key, @RequestBody GlobalSettingValueDto valueDto) throws EbadServiceException {
        GlobalSetting globalSetting = globalSettingService.setValue(key, valueDto.getValue());
        return ResponseEntity.ok(mapper.map(globalSetting, GlobalSettingKeyValueDto.class));
    }

    @GetMapping
    public ResponseEntity<List<GlobalSettingKeyValueDto>> getAllSettings() {
        List<GlobalSetting> globalSettings = globalSettingService.getAllGlobalSettings();
        return ResponseEntity.ok(mapper.mapAsList(globalSettings, GlobalSettingKeyValueDto.class));
    }
}
