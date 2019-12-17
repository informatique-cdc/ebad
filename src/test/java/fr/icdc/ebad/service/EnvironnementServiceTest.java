package fr.icdc.ebad.service;

import com.jcraft.jsch.JSchException;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.plugin.dto.EnvironnementDiscoverDto;
import fr.icdc.ebad.plugin.dto.NormeDiscoverDto;
import fr.icdc.ebad.plugin.plugin.EnvironnementConnectorPlugin;
import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.ChaineRepository;
import fr.icdc.ebad.repository.DirectoryRepository;
import fr.icdc.ebad.repository.EnvironnementRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import fr.icdc.ebad.repository.NormeRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dtrouillet on 10/02/2017.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class EnvironnementServiceTest {
    @MockBean
    private ShellService shellService;
    @MockBean
    private LogBatchRepository logBatchRepository;
    @MockBean
    private ChaineRepository chaineRepository;
    @MockBean
    private BatchRepository batchRepository;
    @MockBean
    private DirectoryRepository directoryRepository;
    @MockBean
    private EnvironnementRepository environnementRepository;
    @MockBean
    private ApplicationRepository applicationRepository;
    @MockBean
    private SpringPluginManager springPluginManager;
    @MockBean
    private NormeRepository normeRepository;
    @MockBean
    private EnvironnementConnectorPlugin environnementConnectorPlugin;

    @Autowired
    private EnvironnementService environnementService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getDateTraiement() throws Exception {
        Norme norme = new Norme();
        norme.setCtrlMDate("parm/ctrlm.date");
        norme.setPathShell("shell/");
        norme.setCommandLine("$1");

        Environnement environnement = new Environnement();
        environnement.setNorme(norme);
        Application application = new Application();
        environnement.setApplication(application);
        RetourBatch retourBatch = new RetourBatch("20160101", 0, 10L);
        when(shellService.runCommand(eq(environnement), anyString())).thenReturn(retourBatch);
        when(environnementRepository.getOne(environnement.getId())).thenReturn(environnement);
        Date dateTraitement = environnementService.getDateTraiement(environnement.getId());
        assertNotNull(dateTraitement);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        assertEquals("01/01/2016", simpleDateFormat.format(dateTraitement));
    }

    @Test
    public void deleteEnvironnement() {
        Set<Batch> batches = new HashSet<>();
        Batch batch1 = new Batch();
        batch1.setId(2L);
        batches.add(batch1);

        Batch batch2 = new Batch();
        batch2.setId(3L);
        batches.add(batch2);

        Environnement environnement = new Environnement();
        environnement.setId(1L);
        environnement.setBatchs(batches);

        doNothing().when(logBatchRepository).deleteByEnvironnement(eq(environnement));
        doNothing().when(chaineRepository).deleteByEnvironnement(eq(environnement));
        doNothing().when(batchRepository).deleteAll(eq(environnement.getBatchs()));
        doNothing().when(directoryRepository).deleteByEnvironnement(eq(environnement));
        doNothing().when(environnementRepository).delete(eq(environnement));

        environnementService.deleteEnvironnement(environnement, true);

        verify(logBatchRepository, times(1)).deleteByEnvironnement(eq(environnement));
        verify(chaineRepository, times(1)).deleteByEnvironnement(eq(environnement));
        verify(batchRepository, times(1)).deleteAll(eq(environnement.getBatchs()));
        verify(directoryRepository, times(1)).deleteByEnvironnement(eq(environnement));
        verify(environnementRepository, times(1)).delete(eq(environnement));
    }

    @Test
    public void deleteEnvironnement2() {
        Set<Batch> batches = new HashSet<>();
        Batch batch1 = new Batch();
        batch1.setId(2L);
        batches.add(batch1);

        Batch batch2 = new Batch();
        batch2.setId(3L);
        batches.add(batch2);


        Environnement environnement = new Environnement();
        environnement.setId(1L);
        environnement.setBatchs(batches);

        doNothing().when(logBatchRepository).deleteByEnvironnement(eq(environnement));
        doNothing().when(chaineRepository).deleteByEnvironnement(eq(environnement));
        doNothing().when(batchRepository).deleteAll(eq(environnement.getBatchs()));
        doNothing().when(directoryRepository).deleteByEnvironnement(eq(environnement));
        doNothing().when(environnementRepository).delete(eq(environnement));

        environnementService.deleteEnvironnement(environnement, false);

        verify(logBatchRepository, times(1)).deleteByEnvironnement(eq(environnement));
        verify(chaineRepository, times(1)).deleteByEnvironnement(eq(environnement));
        verify(batchRepository, times(0)).deleteAll(eq(environnement.getBatchs()));
        verify(directoryRepository, times(1)).deleteByEnvironnement(eq(environnement));
        verify(environnementRepository, times(1)).delete(eq(environnement));
    }

    @Test
    public void testChangeDateTraiement() throws IOException, JSchException {

        RetourBatch retourBatch = new RetourBatch();
        retourBatch.setReturnCode(0);

        Norme norme = Norme.builder().ctrlMDate("date.tr").build();
        Application application = Application.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(1L).homePath("/home").norme(norme).application(application).build();
        when(shellService.runCommand(eq(environnement), eq("echo 01022018 > /home/date.tr"))).thenReturn(retourBatch);
        when(environnementRepository.getOne(eq(environnement.getId()))).thenReturn(environnement);
        boolean result = environnementService.changeDateTraiement(1L, DateTimeFormat.forPattern("ddMMyyyy").parseDateTime("01022018").toDate());

        verify(shellService).runCommand(eq(environnement), eq("echo 01022018 > /home/date.tr"));
        assertTrue(result);
    }

    @Test
    public void testChangeDateTraiementError() throws IOException, JSchException {
        RetourBatch retourBatch = new RetourBatch();
        retourBatch.setReturnCode(0);

        Norme norme = Norme.builder().ctrlMDate("date.tr").build();
        Application application = Application.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(1L).homePath("/home").norme(norme).application(application).build();
        when(shellService.runCommand(eq(environnement), eq("echo 01022018 > /home/date.tr"))).thenThrow(new JSchException());
        when(environnementRepository.getOne(eq(environnement.getId()))).thenReturn(environnement);

        boolean result = environnementService.changeDateTraiement(1L, DateTimeFormat.forPattern("ddMMyyyy").parseDateTime("01022018").toDate());
        assertFalse(result);
    }

    @Test
    public void testGetEspaceDisque() throws IOException, JSchException {

        RetourBatch retourBatch = new RetourBatch();
        retourBatch.setReturnCode(0);
        retourBatch.setLogOut("10%");

        Norme norme = Norme.builder().ctrlMDate("date.tr").build();
        Application application = Application.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(1L).homePath("/home").norme(norme).application(application).build();
        when(shellService.runCommand(eq(environnement), eq("echo $( df -m /home | tail -1 | awk ' { print $4 } ' )"))).thenReturn(retourBatch);
        when(environnementRepository.getOne(eq(environnement.getId()))).thenReturn(environnement);
        String result = environnementService.getEspaceDisque(1L);

        verify(shellService).runCommand(eq(environnement), eq("echo $( df -m /home | tail -1 | awk ' { print $4 } ' )"));
        assertEquals("10", result);
    }

    @Test
    public void testPurgerLog() throws IOException, JSchException {

        RetourBatch retourBatch = new RetourBatch();
        retourBatch.setReturnCode(0);

        Norme norme = Norme.builder().ctrlMDate("date.tr").build();
        Application application = Application.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(1L).homePath("/home").norme(norme).application(application).build();
        when(shellService.runCommand(eq(environnement), eq("find " + environnement.getHomePath() + "/logctm -type f -exec rm -v  {} ';'"))).thenReturn(retourBatch);
        when(environnementRepository.getOne(eq(environnement.getId()))).thenReturn(environnement);
        environnementService.purgerLogs(1L);

        verify(shellService).runCommand(eq(environnement), eq("find " + environnement.getHomePath() + "/logctm -type f -exec rm -v  {} ';'"));
    }

    @Test
    public void testPurgerArchive() throws IOException, JSchException {

        RetourBatch retourBatch = new RetourBatch();
        retourBatch.setReturnCode(0);

        Norme norme = Norme.builder().ctrlMDate("date.tr").build();
        Application application = Application.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(1L).homePath("/home").norme(norme).application(application).build();
        when(shellService.runCommand(eq(environnement), eq("find " + environnement.getHomePath() + "/archive -type f -exec rm -v  {} ';'"))).thenReturn(retourBatch);
        when(environnementRepository.getOne(eq(environnement.getId()))).thenReturn(environnement);
        environnementService.purgerArchive(1L);

        verify(shellService).runCommand(eq(environnement), eq("find " + environnement.getHomePath() + "/archive -type f -exec rm -v  {} ';'"));
    }

    @Test
    public void testUpdateEnvironnement() {
        Environnement environnement = Environnement.builder().id(1L).name("toto").build();
        Environnement environnementUpdated = Environnement.builder().id(1L).name("titi").build();
        when(environnementRepository.getOne(eq(1L))).thenReturn(environnement);
        when(environnementRepository.saveAndFlush(eq(environnementUpdated))).thenReturn(environnementUpdated);
        Environnement result = environnementService.updateEnvironnement(environnementUpdated);
        verify(environnementRepository).saveAndFlush(environnementUpdated);
        assertEquals(environnementUpdated.getName(), result.getName());
    }

    @Test
    public void testImportEnvironments() throws PluginException, EbadServiceException {
        //GIVEN
        Application application = Application.builder()
                .id(1L)
                .code("tt1")
                .build();
        List<Norme> normeList = new ArrayList<>();
        normeList.add(Norme.builder()
                .name("Unix")
                .build());
        normeList.add(Norme.builder()
                .name("Windows")
                .build());

        List<EnvironnementDiscoverDto> discoverDtoList = new ArrayList<>();
        EnvironnementDiscoverDto environnementDiscoverDto1 = EnvironnementDiscoverDto.builder()
                .id("3")
                .host("localhost")
                .name("my environment")
                .code("my")
                .login("root")
                .home("/home/batch")
                .kindOs(EnvironnementDiscoverDto.OsKind.UNIX)
                .norme(NormeDiscoverDto.builder().name("Unix").build())
                .prefix("P")
                .build();
        EnvironnementDiscoverDto environnementDiscoverDto2 = EnvironnementDiscoverDto.builder()
                .id("9")
                .host("10.0.0.1")
                .name("my remote")
                .code("tf")
                .login("loc")
                .home("/home/batch/loc")
                .kindOs(EnvironnementDiscoverDto.OsKind.WINDOWS)
                .norme(NormeDiscoverDto.builder().name("Windows").build())
                .prefix("W")
                .build();
        discoverDtoList.add(environnementDiscoverDto1);
        discoverDtoList.add(environnementDiscoverDto2);

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

        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.of(application));
        when(normeRepository.findAll()).thenReturn(normeList);
        when(environnementConnectorPlugin.discoverFromApp(eq(application.getCode()), anyList())).thenReturn(discoverDtoList);

        when(environnementRepository.findAllByExternalIdAndPluginId(eq(environnementDiscoverDto1.getId()), eq("import-plugin"))).thenReturn(Optional.empty());
        when(environnementRepository.findAllByExternalIdAndPluginId(eq(environnementDiscoverDto2.getId()), eq("import-plugin"))).thenReturn(Optional.empty());

        when(springPluginManager.whichPlugin(any())).thenReturn(pluginWrapper);

        //WHEN
        Set<Environnement> result = environnementService.importEnvironments(1L);

        //THEN
        verify(applicationRepository).findById(eq(1L));
        verify(environnementConnectorPlugin).discoverFromApp(eq(application.getCode()), anyList());
        verify(environnementRepository).findAllByExternalIdAndPluginId(eq(environnementDiscoverDto1.getId()), eq("import-plugin"));
        verify(environnementRepository).findAllByExternalIdAndPluginId(eq(environnementDiscoverDto2.getId()), eq("import-plugin"));

        verify(environnementRepository).save(argThat((env) -> env.getApplication().equals(application)
                && env.getHost().equals(environnementDiscoverDto1.getHost())
                && env.getHomePath().equals(environnementDiscoverDto1.getHome())
                && env.getName().equals(environnementDiscoverDto1.getName())
                && env.getPrefix().equals(environnementDiscoverDto1.getPrefix())
                && env.getExternalId().equals(environnementDiscoverDto1.getId())
                && env.getLogin().equals(environnementDiscoverDto1.getLogin())
                && env.getPluginId().equals("import-plugin")));

        verify(environnementRepository).save(argThat((env) -> env.getApplication().equals(application)
                && env.getHost().equals(environnementDiscoverDto2.getHost())
                && env.getHomePath().equals(environnementDiscoverDto2.getHome())
                && env.getName().equals(environnementDiscoverDto2.getName())
                && env.getPrefix().equals(environnementDiscoverDto2.getPrefix())
                && env.getExternalId().equals(environnementDiscoverDto2.getId())
                && env.getLogin().equals(environnementDiscoverDto2.getLogin())
                && env.getPluginId().equals("import-plugin")));

        List<Environnement> resultList = new ArrayList(result);
        assertEquals(2, resultList.size());

        assertEquals(application, resultList.get(0).getApplication());
        assertEquals(environnementDiscoverDto2.getHost(), resultList.get(0).getHost());
        assertEquals(environnementDiscoverDto2.getHome(), resultList.get(0).getHomePath());
        assertEquals(environnementDiscoverDto2.getName(), resultList.get(0).getName());
        assertEquals(environnementDiscoverDto2.getPrefix(), resultList.get(0).getPrefix());
        assertEquals(environnementDiscoverDto2.getId(), resultList.get(0).getExternalId());
        assertEquals(environnementDiscoverDto2.getLogin(), resultList.get(0).getLogin());
        assertEquals("import-plugin", resultList.get(0).getPluginId());

        assertEquals(application, resultList.get(1).getApplication());
        assertEquals(environnementDiscoverDto1.getHost(), resultList.get(1).getHost());
        assertEquals(environnementDiscoverDto1.getHome(), resultList.get(1).getHomePath());
        assertEquals(environnementDiscoverDto1.getName(), resultList.get(1).getName());
        assertEquals(environnementDiscoverDto1.getPrefix(), resultList.get(1).getPrefix());
        assertEquals(environnementDiscoverDto1.getId(), resultList.get(1).getExternalId());
        assertEquals(environnementDiscoverDto1.getLogin(), resultList.get(1).getLogin());
        assertEquals("import-plugin", resultList.get(1).getPluginId());

    }

    @Test
    public void testImportEnvironmentsAll() throws PluginException, EbadServiceException {
        //GIVEN
        Application application = Application.builder()
                .id(1L)
                .code("tt1")
                .build();

        List<Application> applicationList = new ArrayList<>();
        applicationList.add(application);

        List<Norme> normeList = new ArrayList<>();
        normeList.add(Norme.builder()
                .name("Unix")
                .build());
        normeList.add(Norme.builder()
                .name("Windows")
                .build());

        List<EnvironnementDiscoverDto> discoverDtoList = new ArrayList<>();
        EnvironnementDiscoverDto environnementDiscoverDto1 = EnvironnementDiscoverDto.builder()
                .id("3")
                .host("localhost")
                .name("my environment")
                .code("my")
                .login("root")
                .home("/home/batch")
                .kindOs(EnvironnementDiscoverDto.OsKind.UNIX)
                .norme(NormeDiscoverDto.builder().name("Unix").build())
                .prefix("P")
                .build();
        EnvironnementDiscoverDto environnementDiscoverDto2 = EnvironnementDiscoverDto.builder()
                .id("9")
                .host("10.0.0.1")
                .name("my remote")
                .code("tf")
                .login("loc")
                .home("/home/batch/loc")
                .kindOs(EnvironnementDiscoverDto.OsKind.WINDOWS)
                .norme(NormeDiscoverDto.builder().name("Windows").build())
                .prefix("W")
                .build();
        discoverDtoList.add(environnementDiscoverDto1);
        discoverDtoList.add(environnementDiscoverDto2);

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

        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.of(application));
        when(applicationRepository.findAll()).thenReturn(applicationList);

        when(normeRepository.findAll()).thenReturn(normeList);
        when(environnementConnectorPlugin.discoverFromApp(eq(application.getCode()), anyList())).thenReturn(discoverDtoList);

        when(environnementRepository.findAllByExternalIdAndPluginId(eq(environnementDiscoverDto1.getId()), eq("import-plugin"))).thenReturn(Optional.empty());
        when(environnementRepository.findAllByExternalIdAndPluginId(eq(environnementDiscoverDto2.getId()), eq("import-plugin"))).thenReturn(Optional.empty());

        when(springPluginManager.whichPlugin(any())).thenReturn(pluginWrapper);

        //WHEN
        List<Environnement> resultList = environnementService.importEnvironments();

        //THEN
        verify(applicationRepository).findById(eq(1L));
        verify(environnementConnectorPlugin).discoverFromApp(eq(application.getCode()), anyList());
        verify(environnementRepository).findAllByExternalIdAndPluginId(eq(environnementDiscoverDto1.getId()), eq("import-plugin"));
        verify(environnementRepository).findAllByExternalIdAndPluginId(eq(environnementDiscoverDto2.getId()), eq("import-plugin"));

        verify(environnementRepository).save(argThat((env) -> env.getApplication().equals(application)
                && env.getHost().equals(environnementDiscoverDto1.getHost())
                && env.getHomePath().equals(environnementDiscoverDto1.getHome())
                && env.getName().equals(environnementDiscoverDto1.getName())
                && env.getPrefix().equals(environnementDiscoverDto1.getPrefix())
                && env.getExternalId().equals(environnementDiscoverDto1.getId())
                && env.getLogin().equals(environnementDiscoverDto1.getLogin())
                && env.getPluginId().equals("import-plugin")));

        verify(environnementRepository).save(argThat((env) -> env.getApplication().equals(application)
                && env.getHost().equals(environnementDiscoverDto2.getHost())
                && env.getHomePath().equals(environnementDiscoverDto2.getHome())
                && env.getName().equals(environnementDiscoverDto2.getName())
                && env.getPrefix().equals(environnementDiscoverDto2.getPrefix())
                && env.getExternalId().equals(environnementDiscoverDto2.getId())
                && env.getLogin().equals(environnementDiscoverDto2.getLogin())
                && env.getPluginId().equals("import-plugin")));

        assertEquals(2, resultList.size());

        assertEquals(application, resultList.get(0).getApplication());
        assertEquals(environnementDiscoverDto2.getHost(), resultList.get(0).getHost());
        assertEquals(environnementDiscoverDto2.getHome(), resultList.get(0).getHomePath());
        assertEquals(environnementDiscoverDto2.getName(), resultList.get(0).getName());
        assertEquals(environnementDiscoverDto2.getPrefix(), resultList.get(0).getPrefix());
        assertEquals(environnementDiscoverDto2.getId(), resultList.get(0).getExternalId());
        assertEquals(environnementDiscoverDto2.getLogin(), resultList.get(0).getLogin());
        assertEquals("import-plugin", resultList.get(0).getPluginId());

        assertEquals(application, resultList.get(1).getApplication());
        assertEquals(environnementDiscoverDto1.getHost(), resultList.get(1).getHost());
        assertEquals(environnementDiscoverDto1.getHome(), resultList.get(1).getHomePath());
        assertEquals(environnementDiscoverDto1.getName(), resultList.get(1).getName());
        assertEquals(environnementDiscoverDto1.getPrefix(), resultList.get(1).getPrefix());
        assertEquals(environnementDiscoverDto1.getId(), resultList.get(1).getExternalId());
        assertEquals(environnementDiscoverDto1.getLogin(), resultList.get(1).getLogin());
        assertEquals("import-plugin", resultList.get(1).getPluginId());

    }
}
