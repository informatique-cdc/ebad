package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.QApplication;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.plugin.dto.EnvironnementDiscoverDto;
import fr.icdc.ebad.plugin.plugin.EnvironnementConnectorPlugin;
import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.repository.AuthorityRepository;
import fr.icdc.ebad.repository.TypeFichierRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class ApplicationServiceTest {
    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private EnvironnementService environnementService;

    @MockBean
    private EnvironnementConnectorPlugin environnementConnectorPlugin;

    @Autowired
    ApplicationService applicationService;
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
        doNothing().when(environnementService).deleteEnvironnement(eq(environnement1),eq(true));
        doNothing().when(environnementService).deleteEnvironnement(eq(environnement2),eq(true));
        doNothing().when(applicationRepository).delete(eq(application));

        applicationService.deleteApplication(9L);

        verify(applicationRepository,times(1)).getOne(eq(9L));
        verify(environnementService,times(1)).deleteEnvironnement(eq(environnement1),eq(true));
        verify(environnementService,times(1)).deleteEnvironnement(eq(environnement2),eq(true));
        verify(applicationRepository,times(1)).delete(eq(application));
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

        when(applicationRepository.findAll(any(Sort.class))).thenReturn(applications);

        List<Application> result = applicationService.getAllApplications();

        assertEquals(2, result.size());
        assertTrue(result.contains(application1));
        assertTrue(result.contains(application2));
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
    public void setSaveApplication() {
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

    @Test(expected = EbadServiceException.class)
    public void testImportEnvironmentsError() throws EbadServiceException {
        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.empty());
        applicationService.importEnvironments(1L);
    }

    @Test
    public void testImportEnvironments() throws EbadServiceException {
        Application application = Application.builder().code("myapp").build();
        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.of(application));

        List<EnvironnementDiscoverDto> environnementDiscoverDtos = new ArrayList<>();
        EnvironnementDiscoverDto environnementDiscoverDto1 = EnvironnementDiscoverDto.builder().code("1")
                .home("/home")
                .host("localhost")
                .prefix("I")
                .kindOs(EnvironnementDiscoverDto.OsKind.UNIX)
                .login("root")
                .name("LocalTest")
                .build();
        environnementDiscoverDtos.add(environnementDiscoverDto1);

        when(environnementConnectorPlugin.discoverFromApp(eq("myapp"))).thenReturn(environnementDiscoverDtos);
        List<Environnement> results = applicationService.importEnvironments(1L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("LocalTest", results.get(0).getName());
        assertEquals("/home", results.get(0).getHomePath());
        assertEquals("localhost", results.get(0).getHost());
        assertEquals("root", results.get(0).getLogin());
        assertEquals("I", results.get(0).getPrefix());
        //assertEquals("norme", results.get(0).getNorma());


    }
}
