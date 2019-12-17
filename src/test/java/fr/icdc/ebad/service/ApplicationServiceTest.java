package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.QApplication;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.plugin.dto.ApplicationDiscoverDto;
import fr.icdc.ebad.plugin.plugin.ApplicationConnectorPlugin;
import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class ApplicationServiceTest {
    @Autowired
    ApplicationService applicationService;
    @MockBean
    private ApplicationRepository applicationRepository;
    @MockBean
    private EnvironnementService environnementService;
    @MockBean
    private ApplicationConnectorPlugin applicationConnectorPlugin;
    @MockBean
    private SpringPluginManager springPluginManager;

    @Test
    public void deleteApplication() {
        Environnement environnement1 = new Environnement();
        environnement1.setId(1L);

        Environnement environnement2 = new Environnement();
        environnement2.setId(1L);

        Set<Environnement> environnementSet = new HashSet<>();
        environnementSet.add(environnement1);
        environnementSet.add(environnement2);

        Application application = new Application();
        application.setId(9L);
        application.setEnvironnements(environnementSet);

        when(applicationRepository.getOne(eq(9L))).thenReturn(application);
        doNothing().when(environnementService).deleteEnvironnement(eq(environnement1), eq(true));
        doNothing().when(environnementService).deleteEnvironnement(eq(environnement2), eq(true));
        doNothing().when(applicationRepository).delete(eq(application));

        applicationService.deleteApplication(9L);

        verify(applicationRepository, times(1)).getOne(eq(9L));
        verify(environnementService, times(1)).deleteEnvironnement(eq(environnement1), eq(true));
        verify(environnementService, times(1)).deleteEnvironnement(eq(environnement2), eq(true));
        verify(applicationRepository, times(1)).delete(eq(application));
    }

    @Test
    public void testGetAllApplications() {
        List<Application> applications = new ArrayList<>();
        Application application1 = new Application();
        application1.setId(1L);
        applications.add(application1);

        Application application2 = new Application();
        application2.setId(2L);
        applications.add(application2);

        Pageable pageable = PageRequest.of(0, 2);
        PageImpl<Application> applicationPage = new PageImpl<>(applications);

        when(applicationRepository.findAll(eq(pageable))).thenReturn(applicationPage);

        Page<Application> result = applicationService.getAllApplications(pageable);

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(application1));
        assertTrue(result.getContent().contains(application2));
    }

    @Test
    public void testGetApplication() {
        Application application = new Application();
        application.setId(1L);

        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.of(application));

        Optional<Application> result = applicationService.getApplication(1L);

        assertEquals(application, result.get());
    }

    @Test
    public void testSaveApplication() {
        Application application = new Application();
        application.setId(1L);

        when(applicationRepository.save(eq(application))).thenReturn(application);
        applicationService.saveApplication(application);

        verify(applicationRepository).save(eq(application));
    }

    @Test
    public void testFindApplications() {
        List<Application> applications = new ArrayList<>();
        Application application1 = Application.builder().id(1L).build();

        applications.add(application1);

        Predicate predicate = QApplication.application.id.eq(1L);
        when(applicationRepository.findAll(eq(predicate))).thenReturn(applications);

        List<Application> results = applicationService.findApplication(predicate);

        verify(applicationRepository).findAll(eq(predicate));
        assertEquals(1, results.size());
        assertEquals(results.get(0), application1);
    }

    @Test
    public void testGetManagersEmpty() {
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());
        Set<User> results = applicationService.getManagers(1L);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetManagers() {
        Set<UsageApplication> usageApplications = new HashSet<>();
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(2L);
        UsageApplication usageApplication1 = new UsageApplication(null, user1, null, true, false);
        UsageApplication usageApplication2 = new UsageApplication(null, user2, null, false, false);
        UsageApplication usageApplication3 = new UsageApplication(null, user3, null, false, true);
        usageApplications.add(usageApplication1);
        usageApplications.add(usageApplication2);
        usageApplications.add(usageApplication3);

        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.of(Application.builder().id(1L).usageApplications(usageApplications).build()));
        Set<User> results = applicationService.getManagers(1L);

        assertFalse(results.isEmpty());
        assertTrue(results.contains(user1));
    }

    @Test
    public void testGetUsersEmpty() {
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());
        Set<User> results = applicationService.getUsers(1L);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetUsers() {
        Set<UsageApplication> usageApplications = new HashSet<>();
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(2L);
        UsageApplication usageApplication1 = new UsageApplication(null, user1, null, true, false);
        UsageApplication usageApplication2 = new UsageApplication(null, user2, null, false, false);
        UsageApplication usageApplication3 = new UsageApplication(null, user3, null, false, true);
        usageApplications.add(usageApplication1);
        usageApplications.add(usageApplication2);
        usageApplications.add(usageApplication3);

        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.of(Application.builder().id(1L).usageApplications(usageApplications).build()));
        Set<User> results = applicationService.getUsers(1L);

        assertFalse(results.isEmpty());
        assertTrue(results.contains(user3));
    }

    @Test
    public void testImportApp() throws PluginException {
        List<ApplicationDiscoverDto> applicationDiscoverDtoList = new ArrayList<>();
        ApplicationDiscoverDto applicationDiscoverDto1 = ApplicationDiscoverDto.builder()
                .code("aa2")
                .id("2")
                .name("appa")
                .build();
        ApplicationDiscoverDto applicationDiscoverDto2 = ApplicationDiscoverDto.builder()
                .code("aa3")
                .id("3")
                .name("appa3")
                .build();

        applicationDiscoverDtoList.add(applicationDiscoverDto1);
        applicationDiscoverDtoList.add(applicationDiscoverDto2);

        PluginDescriptor pluginDescriptor = new PluginDescriptor() {
            @Override
            public String getPluginId() {
                return "import-plugin";
            }

            @Override
            public String getPluginDescription() {
                return null;
            }

            @Override
            public String getPluginClass() {
                return null;
            }

            @Override
            public String getVersion() {
                return null;
            }

            @Override
            public String getRequires() {
                return null;
            }

            @Override
            public String getProvider() {
                return null;
            }

            @Override
            public String getLicense() {
                return null;
            }

            @Override
            public List<PluginDependency> getDependencies() {
                return null;
            }
        };
        PluginWrapper pluginWrapper = new PluginWrapper(springPluginManager, pluginDescriptor, null, null);
        when(springPluginManager.whichPlugin(any())).thenReturn(pluginWrapper);

        when(applicationRepository.findAllByExternalIdAndPluginId(eq("2"), eq("import-plugin"))).thenReturn(Optional.empty());
        when(applicationConnectorPlugin.discoverApp()).thenReturn(applicationDiscoverDtoList);
        applicationService.importApp();
        verify(applicationRepository).save(argThat((app) ->
                app.getName().equals(applicationDiscoverDto1.getName())
                        && app.getCode().equals(applicationDiscoverDto1.getCode())
                        && app.getExternalId().equals(applicationDiscoverDto1.getId())
        ));
        verify(applicationRepository).save(argThat((app) ->
                app.getName().equals(applicationDiscoverDto2.getName())
                        && app.getCode().equals(applicationDiscoverDto2.getCode())
                        && app.getExternalId().equals(applicationDiscoverDto2.getId())
        ));
    }

    @Test
    public void testUpdateApplication() throws EbadServiceException {
        Application oldApplication = Application.builder()
                .id(1L)
                .name("test")
                .code("154")
                .dateParametrePattern("ddMMyyyyy")
                .dateFichierPattern("ddMMyyyyy")
                .build();
        Application newApplication = Application.builder()
                .id(1L)
                .name("news")
                .code("548")
                .dateParametrePattern("yyyy-MM-dd")
                .dateFichierPattern("yyyy-dd-MM")
                .build();
        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.of(oldApplication));
        when(applicationRepository.save(eq(newApplication))).thenReturn(newApplication);
        Application result = applicationService.updateApplication(newApplication);

        verify(applicationRepository).save(argThat((app) ->
                app.getId().equals(newApplication.getId())
                        && app.getCode().equals(newApplication.getCode())
                        && app.getName().equals(newApplication.getName())
                        && app.getDateParametrePattern().equals(newApplication.getDateParametrePattern())
                        && app.getDateFichierPattern().equals(newApplication.getDateFichierPattern())
        ));

        assertEquals(newApplication.getId(), result.getId());
        assertEquals(newApplication.getCode(), result.getCode());
        assertEquals(newApplication.getName(), result.getName());
        assertEquals(newApplication.getDateParametrePattern(), result.getDateParametrePattern());
        assertEquals(newApplication.getDateFichierPattern(), result.getDateFichierPattern());
    }
}
