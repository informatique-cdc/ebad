package fr.icdc.ebad.service;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.GlobalSetting;
import fr.icdc.ebad.repository.GlobalSettingRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class GlobalSettingServiceTest {
    @MockBean
    private GlobalSettingRepository globalSettingRepository;

    @Autowired
    private GlobalSettingService globalSettingService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void saveGlobalSetting() throws EbadServiceException {
        GlobalSetting globalSettingSaved = new GlobalSetting("TEST_KEY", "TEST_VALUE", "TEST_LAVEL", "TEST_DESCRIPTION");
        when(globalSettingRepository.findById(eq(globalSettingSaved.getKey()))).thenReturn(Optional.empty());
        when(globalSettingRepository.save(argThat((globalSetting ->
                globalSettingSaved.getValue().equals(globalSetting.getValue())
                        && globalSettingSaved.getKey().equals(globalSetting.getKey())
                        && globalSettingSaved.getLabel().equals(globalSetting.getLabel())
                        && globalSettingSaved.getDescription().equals(globalSetting.getDescription())
        )))).thenReturn(globalSettingSaved);

        globalSettingService.saveGlobalSetting(globalSettingSaved.getKey(), globalSettingSaved.getValue(), globalSettingSaved.getDescription(), globalSettingSaved.getLabel());

        verify(globalSettingRepository).save(argThat((globalSetting ->
                globalSettingSaved.getValue().equals(globalSetting.getValue())
                        && globalSettingSaved.getKey().equals(globalSetting.getKey())
                        && globalSettingSaved.getLabel().equals(globalSetting.getLabel())
                        && globalSettingSaved.getDescription().equals(globalSetting.getDescription())
        )));
    }

    @Test(expected = EbadServiceException.class)
    public void saveGlobalSettingAlreadyExist() throws EbadServiceException {
        GlobalSetting globalSettingSaved = new GlobalSetting("TEST_KEY", "TEST_VALUE", "TEST_LAVEL", "TEST_DESCRIPTION");
        when(globalSettingRepository.findById(eq(globalSettingSaved.getKey()))).thenReturn(Optional.of(globalSettingSaved));
        globalSettingService.saveGlobalSetting(globalSettingSaved.getKey(), globalSettingSaved.getValue(), globalSettingSaved.getDescription(), globalSettingSaved.getLabel());
    }

    @Test
    public void setValue() throws EbadServiceException {
        GlobalSetting globalSettingOld = new GlobalSetting("TEST_KEY", "TEST_VALUE", "TEST_LAVEL", "TEST_DESCRIPTION");
        GlobalSetting globalSettingNew = new GlobalSetting("TEST_KEY", "TEST_VALUE_NEW", "TEST_LAVEL", "TEST_DESCRIPTION");
        when(globalSettingRepository.findById(eq(globalSettingOld.getKey()))).thenReturn(Optional.of(globalSettingOld));
        when(globalSettingRepository.save(argThat((globalSetting ->
                globalSettingNew.getValue().equals(globalSetting.getValue())
                        && globalSettingNew.getKey().equals(globalSetting.getKey())
                        && globalSettingNew.getLabel().equals(globalSetting.getLabel())
                        && globalSettingNew.getDescription().equals(globalSetting.getDescription())
        )))).thenReturn(globalSettingOld);

        globalSettingService.setValue(globalSettingNew.getKey(), globalSettingNew.getValue());

        verify(globalSettingRepository).save(argThat((globalSetting ->
                globalSettingNew.getValue().equals(globalSetting.getValue())
                        && globalSettingNew.getKey().equals(globalSetting.getKey())
                        && globalSettingNew.getLabel().equals(globalSetting.getLabel())
                        && globalSettingNew.getDescription().equals(globalSetting.getDescription())
        )));
    }

    @Test(expected = EbadServiceException.class)
    public void setValueNotExist() throws EbadServiceException {
        GlobalSetting globalSettingNew = new GlobalSetting("TEST_KEY", "TEST_VALUE_NEW", "TEST_LAVEL", "TEST_DESCRIPTION");
        when(globalSettingRepository.findById(eq(globalSettingNew.getKey()))).thenReturn(Optional.empty());
        globalSettingService.setValue(globalSettingNew.getKey(), globalSettingNew.getValue());
    }

    @Test
    public void getValue() throws EbadServiceException {
        GlobalSetting globalSetting = new GlobalSetting("TEST_KEY", "TEST_VALUE_NEW", "TEST_LAVEL", "TEST_DESCRIPTION");
        when(globalSettingRepository.findById(eq(globalSetting.getKey()))).thenReturn(Optional.of(globalSetting));
        GlobalSetting result = globalSettingService.getValue(globalSetting.getKey());
        assertEquals(globalSetting, result);
    }

    @Test(expected = EbadServiceException.class)
    public void getValueNotExist() throws EbadServiceException {
        when(globalSettingRepository.findById(eq("KEY"))).thenReturn(Optional.empty());
        globalSettingService.getValue("KEY");
    }

    @Test
    public void getAllGlobalSettings() {
        GlobalSetting globalSetting1 = new GlobalSetting("TEST_KEY1", "TEST_VALUE_NEW1", "TEST_LAVEL1", "TEST_DESCRIPTION1");
        GlobalSetting globalSetting2 = new GlobalSetting("TEST_KEY2", "TEST_VALUE_NEW2", "TEST_LAVEL2", "TEST_DESCRIPTION2");
        List<GlobalSetting> globalSettingList = new ArrayList<>();
        globalSettingList.add(globalSetting1);
        globalSettingList.add(globalSetting2);

        when(globalSettingRepository.findAll()).thenReturn(globalSettingList);

        List<GlobalSetting> result = globalSettingService.getAllGlobalSettings();

        assertEquals(2, result.size());
        assertEquals(globalSetting1, result.get(0));
        assertEquals(globalSetting2, result.get(1));
    }
}
