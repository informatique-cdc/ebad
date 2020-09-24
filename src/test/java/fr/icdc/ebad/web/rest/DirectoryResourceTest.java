package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.security.permission.PermissionDirectory;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.service.DirectoryService;
import fr.icdc.ebad.web.rest.dto.FilesDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @MockBean
    PermissionEnvironnement permissionEnvironnement;

    @Autowired
    DirectoryResource directoryResource;

    private MockMvc restMvc;

    @Autowired
    private QuerydslPredicateArgumentResolver querydslPredicateArgumentResolver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(directoryResource)
                .setCustomArgumentResolvers(querydslPredicateArgumentResolver, new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    public void getFilesFromDirectory() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/directories/files/1");

        FilesDto filesDTO = new FilesDto();
        filesDTO.setName("toto");
        filesDTO.setSubDirectory("subDir5");
        when(directoryService.listAllFiles(eq(1L), anyString())).thenReturn(Collections.singletonList(filesDTO));
        when(permissionDirectory.canRead(eq(1L), any(), any(UserDetails.class))).thenReturn(true);
        restMvc.perform(builder)
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    public void getAllFromEnv() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/directories/env/1").principal(mockPrincipal);

        List<Directory> directoryList = new ArrayList<>();
        Directory directory1 = Directory.builder().id(2L).name("directory1").build();
        Directory directory2 = Directory.builder().id(3L).name("directory2").build();
        directoryList.add(directory1);
        directoryList.add(directory2);
        Page<Directory> directoryPage = new PageImpl<>(directoryList);

        when(directoryService.findDirectoryFromEnvironnement(any(), any(), eq(1L))).thenReturn(directoryPage);
        when(permissionEnvironnement.canRead(eq(1L), any())).thenReturn(true);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(2)))
                .andExpect(jsonPath("$.content[0].name", is("directory1")))
                .andExpect(jsonPath("$.content[1].id", is(3)))
                .andExpect(jsonPath("$.content[1].name", is("directory2")));
    }
}
