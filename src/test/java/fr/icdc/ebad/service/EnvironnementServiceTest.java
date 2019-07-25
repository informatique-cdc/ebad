package fr.icdc.ebad.service;

import com.jcraft.jsch.JSchException;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.ChaineRepository;
import fr.icdc.ebad.repository.DirectoryRepository;
import fr.icdc.ebad.repository.EnvironnementRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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
}
