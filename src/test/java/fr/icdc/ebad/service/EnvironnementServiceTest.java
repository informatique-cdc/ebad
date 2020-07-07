package fr.icdc.ebad.service;

import com.jcraft.jsch.JSchException;
import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.QEnvironnement;
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
import ma.glasnost.orika.MapperFacade;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
@RunWith(MockitoJUnitRunner.class)
public class EnvironnementServiceTest {
    @Mock
    private ShellService shellService;
    @Mock
    private LogBatchRepository logBatchRepository;
    @Mock
    private ChaineRepository chaineRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private DirectoryRepository directoryRepository;
    @Mock
    private EnvironnementRepository environnementRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private SpringPluginManager springPluginManager;
    @Mock
    private NormeRepository normeRepository;
    @Mock
    private EnvironnementConnectorPlugin environnementConnectorPlugin;
    @Spy
    private MapperFacade mapperFacade;
    @Spy
    private List<EnvironnementConnectorPlugin> environnementConnectorPluginList = new ArrayList<>();

    @InjectMocks
    private EnvironnementService environnementService;

    @Before
    public void setup() {
        environnementConnectorPluginList.add(environnementConnectorPlugin);
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
    public void testImportEnvironments() throws PluginRuntimeException, EbadServiceException {
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
        when(environnementConnectorPlugin.discoverFromApp(eq(application.getCode()), eq(application.getName()), anyList())).thenReturn(discoverDtoList);

        when(environnementRepository.findAllByExternalIdAndPluginId(eq(environnementDiscoverDto1.getId()), eq("import-plugin"))).thenReturn(Optional.empty());
        when(environnementRepository.findAllByExternalIdAndPluginId(eq(environnementDiscoverDto2.getId()), eq("import-plugin"))).thenReturn(Optional.empty());

        when(springPluginManager.whichPlugin(any())).thenReturn(pluginWrapper);

        //WHEN
        Set<Environnement> result = environnementService.importEnvironments(1L);

        //THEN
        verify(applicationRepository).findById(eq(1L));
        verify(environnementConnectorPlugin).discoverFromApp(eq(application.getCode()), eq(application.getName()), anyList());
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


        Environnement expectedEnv1 = Environnement.builder()
                .externalId(environnementDiscoverDto1.getId())
                .host(environnementDiscoverDto1.getHost())
                .homePath(environnementDiscoverDto1.getHome())
                .name(environnementDiscoverDto1.getName())
                .prefix(environnementDiscoverDto1.getPrefix())
                .login(environnementDiscoverDto1.getLogin())
                .application(application)
                .pluginId("import-plugin")
                .build();

        Environnement expectedEnv2 = Environnement.builder()
                .externalId(environnementDiscoverDto2.getId())
                .host(environnementDiscoverDto2.getHost())
                .homePath(environnementDiscoverDto2.getHome())
                .name(environnementDiscoverDto2.getName())
                .prefix(environnementDiscoverDto2.getPrefix())
                .login(environnementDiscoverDto2.getLogin())
                .application(application)
                .pluginId("import-plugin")
                .build();

        List<Environnement> resultList = new ArrayList(result);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(expectedEnv1));
        assertTrue(resultList.contains(expectedEnv2));

    }

    @Test
    public void testImportEnvironmentsError() throws PluginRuntimeException, EbadServiceException {
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
        when(environnementConnectorPlugin.discoverFromApp(eq(application.getCode()), eq(application.getName()), anyList())).thenThrow(new PluginRuntimeException());


        when(springPluginManager.whichPlugin(any())).thenReturn(pluginWrapper);

        //WHEN
        Set<Environnement> result = environnementService.importEnvironments(1L);

        //THEN
        verify(applicationRepository).findById(eq(1L));
        verify(environnementConnectorPlugin).discoverFromApp(eq(application.getCode()), eq(application.getName()), anyList());

        List<Environnement> resultList = new ArrayList(result);
        assertTrue(resultList.isEmpty());

    }

    @Test
    public void testImportEnvironmentsAll() throws PluginRuntimeException, EbadServiceException {
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
        when(environnementConnectorPlugin.discoverFromApp(eq(application.getCode()), eq(application.getName()), anyList())).thenReturn(discoverDtoList);

        when(environnementRepository.findAllByExternalIdAndPluginId(eq(environnementDiscoverDto1.getId()), eq("import-plugin"))).thenReturn(Optional.empty());
        when(environnementRepository.findAllByExternalIdAndPluginId(eq(environnementDiscoverDto2.getId()), eq("import-plugin"))).thenReturn(Optional.empty());

        when(springPluginManager.whichPlugin(any())).thenReturn(pluginWrapper);

        //WHEN
        List<Environnement> resultList = environnementService.importEnvironments();

        //THEN
        verify(applicationRepository).findById(eq(1L));
        verify(environnementConnectorPlugin).discoverFromApp(eq(application.getCode()), eq(application.getName()), anyList());
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

        Environnement expectedEnv1 = Environnement.builder()
                .externalId(environnementDiscoverDto1.getId())
                .host(environnementDiscoverDto1.getHost())
                .homePath(environnementDiscoverDto1.getHome())
                .name(environnementDiscoverDto1.getName())
                .prefix(environnementDiscoverDto1.getPrefix())
                .login(environnementDiscoverDto1.getLogin())
                .application(application)
                .pluginId("import-plugin")
                .build();

        Environnement expectedEnv2 = Environnement.builder()
                .externalId(environnementDiscoverDto2.getId())
                .host(environnementDiscoverDto2.getHost())
                .homePath(environnementDiscoverDto2.getHome())
                .name(environnementDiscoverDto2.getName())
                .prefix(environnementDiscoverDto2.getPrefix())
                .login(environnementDiscoverDto2.getLogin())
                .application(application)
                .pluginId("import-plugin")
                .build();

        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(expectedEnv1));
        assertTrue(resultList.contains(expectedEnv2));

    }

    @Test
    public void testGetEnvironmentFromApp() {
        Application application = Application.builder().id(1L).build();
        Environnement environnement1 = Environnement.builder()
                .application(application)
                .name("myEnv1")
                .build();
        Environnement environnement2 = Environnement.builder()
                .application(application)
                .name("myEnv2")
                .build();
        List<Environnement> environnementList = new ArrayList<>();
        environnementList.add(environnement1);
        environnementList.add(environnement2);
        Page<Environnement> environnementPage = new PageImpl<>(environnementList);
        Pageable page = PageRequest.of(0, 2);
        when(environnementRepository.findAll(any(Predicate.class), eq(page))).thenReturn(environnementPage);

        Page<Environnement> resultPage = environnementService.getEnvironmentFromApp(1L, QEnvironnement.environnement.id.eq(1L), page);
        List<Environnement> resultList = resultPage.getContent();
        verify(environnementRepository).findAll(any(Predicate.class), eq(page));

        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(environnement1));
        assertTrue(resultList.contains(environnement2));
    }
}
