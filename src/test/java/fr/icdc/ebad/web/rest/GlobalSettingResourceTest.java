package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.GlobalSetting;
import fr.icdc.ebad.service.GlobalSettingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class GlobalSettingResourceTest {
    @MockBean
    private GlobalSettingService globalSettingService;

    @Autowired
    private GlobalSettingResource globalSettingResource;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(globalSettingResource).build();
    }

    @Test
    public void getValue() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/global-settings/TEST_KEY");
        GlobalSetting globalSetting = new GlobalSetting("TEST_KEY", "TEST_VALUE", "TEST_LAVEL", "TEST_DESCRIPTION");
        when(globalSettingService.getValue(eq("TEST_KEY"))).thenReturn(globalSetting);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", is("TEST_KEY")))
                .andExpect(jsonPath("$.value", is("TEST_VALUE")));
    }

    @Test
    public void getAllSettings() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/global-settings");
        GlobalSetting globalSetting1 = new GlobalSetting("TEST_KEY1", "TEST_VALUE1", "TEST_LAVEL1", "TEST_DESCRIPTION1");
        GlobalSetting globalSetting2 = new GlobalSetting("TEST_KEY2", "TEST_VALUE2", "TEST_LAVEL2", "TEST_DESCRIPTION2");
        List<GlobalSetting> globalSettingList = new ArrayList<>();
        globalSettingList.add(globalSetting1);
        globalSettingList.add(globalSetting2);
        when(globalSettingService.getAllGlobalSettings()).thenReturn(globalSettingList);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].key", is("TEST_KEY1")))
                .andExpect(jsonPath("$[1].key", is("TEST_KEY2")))
                .andExpect(jsonPath("$[0].value", is("TEST_VALUE1")))
                .andExpect(jsonPath("$[1].value", is("TEST_VALUE2")));
    }
}
