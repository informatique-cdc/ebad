package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.GlobalSetting;
import fr.icdc.ebad.service.GlobalSettingService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.GlobalSettingKeyValueDto;
import fr.icdc.ebad.web.rest.dto.GlobalSettingValueDto;
import fr.icdc.ebad.web.rest.mapstruct.MapStructMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/global-settings")
@Tag(name = "Global Setting", description = "the global setting request API")
public class GlobalSettingResource {
    private final GlobalSettingService globalSettingService;
    private final MapStructMapper mapStructMapper;

    public GlobalSettingResource(GlobalSettingService globalSettingService, MapStructMapper mapStructMapper) {
        this.globalSettingService = globalSettingService;
        this.mapStructMapper = mapStructMapper;
    }

    @GetMapping("/{key}")
    public ResponseEntity<GlobalSettingKeyValueDto> getValue(@PathVariable String key) throws EbadServiceException {
        GlobalSetting globalSetting = globalSettingService.getValue(key);
        return ResponseEntity.ok(mapStructMapper.convert(globalSetting));
    }

    @PostMapping("/{key}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<GlobalSettingKeyValueDto> setValue(@PathVariable String key, @RequestBody GlobalSettingValueDto valueDto) throws EbadServiceException {
        GlobalSetting globalSetting = globalSettingService.setValue(key, valueDto.getValue());
        return ResponseEntity.ok(mapStructMapper.convert(globalSetting));
    }

    @GetMapping
    public ResponseEntity<List<GlobalSettingKeyValueDto>> getAllSettings() {
        List<GlobalSetting> globalSettings = globalSettingService.getAllGlobalSettings();
        return ResponseEntity.ok(mapStructMapper.convertToGlobalSettingKeyValueDtoList(globalSettings));
    }
}
