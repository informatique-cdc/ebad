package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.security.PermissionDirectory;
import fr.icdc.ebad.service.DirectoryService;
import fr.icdc.ebad.web.rest.dto.FilesDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by dtrouillet on 06/03/2018.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class DirectoryResourceTest {
    @MockBean
    DirectoryService directoryService;

    @MockBean
    PermissionDirectory permissionDirectory;

    @Autowired
    DirectoryResource directoryResource;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(directoryResource).build();
    }

    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    public void getFilesFromDirectory() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/directories/files/1");

        FilesDto filesDTO = new FilesDto();
        filesDTO.setName("toto");
        when(directoryService.listAllFiles(eq(1L))).thenReturn(Collections.singletonList(filesDTO));
        when(permissionDirectory.canRead(eq(1L),any(UserDetails.class))).thenReturn(true);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andDo(print());
    }

}
