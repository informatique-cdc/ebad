package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.security.permission.PermissionIdentity;
import fr.icdc.ebad.service.IdentityService;
import fr.icdc.ebad.util.TestUtil;
import fr.icdc.ebad.web.rest.dto.CompleteIdentityDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class IdentityResourceTest {
    private MockMvc restMvc;

    @MockBean
    private IdentityService identityService;

    @MockBean
    private PermissionIdentity permissionIdentity;

    @Autowired
    private IdentityResource identityResource;


    @Before
    public void setup() {
        this.restMvc = MockMvcBuilders
                .standaloneSetup(identityResource)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @WithMockUser
    public void addIdentity() throws Exception {
        when(permissionIdentity.canWriteByApplication(isNull(), any())).thenReturn(true);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/identities");

        CompleteIdentityDto identityDto = CompleteIdentityDto.builder().name("nameTest").login("loginTest").build();
        Identity identity = Identity.builder().id(1L).name("nameTest").login("loginTest").build();
        when(identityService.saveIdentity(any(Identity.class))).thenReturn(identity);
        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(identityDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("nameTest")))
                .andExpect(jsonPath("$.login", is("loginTest")));

        ArgumentCaptor<Identity> identityArgumentCaptor = ArgumentCaptor.forClass(Identity.class);
        verify(identityService, times(1)).saveIdentity(identityArgumentCaptor.capture());
        assertEquals("nameTest", identityArgumentCaptor.getValue().getName());
        assertEquals("loginTest", identityArgumentCaptor.getValue().getLogin());
    }

    @Test
    @WithMockUser
    public void getOneIdentity() throws Exception {
        when(permissionIdentity.canWrite(eq(1L), any())).thenReturn(true);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/identities/1");

        Identity identity = Identity.builder().id(1L).name("nameTest").login("loginTest").build();
        when(identityService.getIdentity(eq(1L))).thenReturn(Optional.of(identity));
        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("nameTest")))
                .andExpect(jsonPath("$.login", is("loginTest")));

        verify(identityService, times(1)).getIdentity(eq(1L));
    }

    @Test
    @WithMockUser
    public void getAllIdentities() throws Exception {
        when(permissionIdentity.canReadByApplication(isNull(), any())).thenReturn(true);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/identities");

        Identity identity1 = Identity.builder().id(1L).name("nameTest1").login("loginTest1").build();
        Identity identity2 = Identity.builder().id(2L).name("nameTest2").login("loginTest2").build();
        Identity identity3 = Identity.builder().id(3L).name("nameTest3").login("loginTest3").build();

        List<Identity> identities = new ArrayList<>();
        identities.add(identity1);
        identities.add(identity2);
        identities.add(identity3);

        Page<Identity>  identityPage = new PageImpl<>(identities);
        when(identityService.findWithoutApp(any(Pageable.class))).thenReturn(identityPage);

        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("nameTest1")))
                .andExpect(jsonPath("$.content[0].login", is("loginTest1")))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].name", is("nameTest2")))
                .andExpect(jsonPath("$.content[1].login", is("loginTest2")))
                .andExpect(jsonPath("$.content[2].id", is(3)))
                .andExpect(jsonPath("$.content[2].name", is("nameTest3")))
                .andExpect(jsonPath("$.content[2].login", is("loginTest3")));
    }

    @Test
    @WithMockUser
    public void getAllIdentitiesWithApplication() throws Exception {
        when(permissionIdentity.canReadByApplication(eq(1L), any())).thenReturn(true);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/identities?applicationId=1");

        Identity identity1 = Identity.builder().id(1L).name("nameTest1").login("loginTest1").build();
        Identity identity2 = Identity.builder().id(2L).name("nameTest2").login("loginTest2").build();
        Identity identity3 = Identity.builder().id(3L).name("nameTest3").login("loginTest3").build();

        List<Identity> identities = new ArrayList<>();
        identities.add(identity1);
        identities.add(identity2);
        identities.add(identity3);

        Page<Identity>  identityPage = new PageImpl<>(identities);
        when(identityService.findAllByApplication(eq(1L), any(Pageable.class))).thenReturn(identityPage);

        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("nameTest1")))
                .andExpect(jsonPath("$.content[0].login", is("loginTest1")))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].name", is("nameTest2")))
                .andExpect(jsonPath("$.content[1].login", is("loginTest2")))
                .andExpect(jsonPath("$.content[2].id", is(3)))
                .andExpect(jsonPath("$.content[2].name", is("nameTest3")))
                .andExpect(jsonPath("$.content[2].login", is("loginTest3")));
    }

    @Test
    @WithMockUser
    public void updateIdentity() throws Exception {
        when(permissionIdentity.canWriteByApplication(eq(2L), any())).thenReturn(true);
        when(permissionIdentity.canWrite(eq(1L), any())).thenReturn(true);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/identities");

        CompleteIdentityDto identityDto = CompleteIdentityDto.builder().id(1L).availableApplication(2L).name("nameTestNew").login("loginTestNew").build();

        Application application = Application.builder().id(2L).build();
        Identity identity = Identity.builder().id(1L).availableApplication(application).name("nameTestNew").login("loginTestNew").build();
        when(identityService.saveIdentity(any(Identity.class))).thenReturn(identity);

        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(identityDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.availableApplication", is(2)))
                .andExpect(jsonPath("$.name", is("nameTestNew")))
                .andExpect(jsonPath("$.login", is("loginTestNew")));

        ArgumentCaptor<Identity> identityArgumentCaptor = ArgumentCaptor.forClass(Identity.class);
        verify(identityService, times(1)).saveIdentity(identityArgumentCaptor.capture());
        assertEquals("nameTestNew", identityArgumentCaptor.getValue().getName());
        assertEquals("loginTestNew", identityArgumentCaptor.getValue().getLogin());
        assertEquals(2L, identityArgumentCaptor.getValue().getAvailableApplication().getId(),0);

    }

    @Test
    @WithMockUser
    public void deleteIdentity() throws Exception {
        when(permissionIdentity.canWrite(eq(1L), any())).thenReturn(true);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/identities/1");
        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
        verify(identityService, times(1)).deleteIdentity(eq(1L));
    }
}