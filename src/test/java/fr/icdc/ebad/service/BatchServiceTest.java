package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.*;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import fr.icdc.ebad.repository.SchedulingRepository;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by DTROUILLET on 12/03/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class BatchServiceTest {
    @Mock
    private ShellService shellService;

    @Mock
    private EnvironnementService environnementService;

    @Mock
    private UserService userService;

    @Mock
    private NormeService normeService;

    @Mock
    private LogBatchRepository logBatchRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SchedulingRepository schedulingRepository;

    @Mock
    private JobScheduler jobScheduler;

    @InjectMocks
    private BatchService batchService;

    @Test
    public void runBatch() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        String commandExpected = "/home/app1/shell/Itest.ksh 20180201 toto ";

        User user = new User();
        user.setId(1L);


        Batch batch = new Batch();
        batch.setPath("test.ksh");
        batch.setName("testName");
        batch.setId(1L);
        batch.setParams("${DATE_TRAITEMENT} toto");

        Norme norme = new Norme();
        norme.setName("norme 1");
        norme.setPathShell("shell/");
        norme.setCommandLine("/bin/bash $1");

        Application application = new Application();
        application.setId(1L);
        application.setDateParametrePattern("yyyyMMdd");
        application.setCode("AA1");

        Environnement environnementIntegration = new Environnement();
        environnementIntegration.setId(1L);
        environnementIntegration.setPrefix("I");
        environnementIntegration.setApplication(application);
        environnementIntegration.setHomePath("/home/app1");
        environnementIntegration.setNorme(norme);
        environnementIntegration.setName("testEnv");

        RetourBatch retourBatch = new RetourBatch();
        retourBatch.setReturnCode(5);


        LogBatch logBatchExpected = new LogBatch();
        logBatchExpected.setId(1L);
        when(environnementService.getDateTraiement(
                eq(environnementIntegration.getId()))
        ).thenReturn(simpleDateFormat.parse("01/02/2018"));

        when(shellService.runCommandNew(
                eq(environnementIntegration), eq(commandExpected))
        ).thenReturn(retourBatch);

        when(logBatchRepository.save(
                argThat(logBatch -> {
                            try {
                                return logBatch.getEnvironnement().getId().equals(environnementIntegration.getId())
                                        && logBatch.getBatch().getId().equals(batch.getId())
                                        && logBatch.getDateTraitement().equals(simpleDateFormat.parse("01/02/2018"))
                                        && logBatch.getUser().getId().equals(user.getId())
                                        && logBatch.getReturnCode() == retourBatch.getReturnCode();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }
                )
        )).thenReturn(logBatchExpected);

        doNothing().when(notificationService).createNotification(any(), any(), eq(true));
        when(normeService.getShellPath(eq(norme), eq("AA1"))).thenReturn(norme.getPathShell());
        when(batchRepository.getOne(batch.getId())).thenReturn(batch);
        when(userService.getUser("user")).thenReturn(Optional.of(user));
        when(environnementService.getEnvironnement(environnementIntegration.getId())).thenReturn(environnementIntegration);
        batchService.jobRunBatch(batch.getId(), environnementIntegration.getId(), null, "user");

        verify(environnementService, times(1)).getDateTraiement(
                argThat(environnement -> environnementIntegration.getId().equals(environnement))
        );

        verify(shellService, times(1)).runCommandNew(
                argThat(environnement -> environnement.getId().equals(environnementIntegration.getId())
                ), eq(commandExpected)
        );

        verify(logBatchRepository,times(1)).save(
                argThat(logBatch -> {
                            try {
                                return logBatch.getEnvironnement().getId().equals(environnementIntegration.getId())
                                        && logBatch.getBatch().getId().equals(batch.getId())
                                        && logBatch.getDateTraitement().equals(simpleDateFormat.parse("01/02/2018"))
                                        && logBatch.getUser().getId().equals(user.getId())
                                        && logBatch.getReturnCode() == retourBatch.getReturnCode();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }
                )
        );

        verify(notificationService, times(1)).createNotification("[AA1] Le batch testName sur l'environnement testEnv vient de se terminer avec le code retour 5", user, true);
    }

    @Test
    public void runBatch2() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        String commandExpected = "/home/app1/shell/Itest.ksh 20180201 toto ";

        User user = new User();
        user.setId(1L);


        Batch batch = new Batch();
        batch.setPath("test.ksh");
        batch.setName("testName");
        batch.setId(1L);
        batch.setParams("${DATE_TRAITEMENT} toto");

        Norme norme = new Norme();
        norme.setName("norme 1");
        norme.setPathShell("shell/");
        norme.setCommandLine("/bin/bash $1");

        Application application = new Application();
        application.setId(1L);
        application.setDateParametrePattern("yyyyMMdd");
        application.setCode("AA1");

        Environnement environnementIntegration = new Environnement();
        environnementIntegration.setId(1L);
        environnementIntegration.setPrefix("I");
        environnementIntegration.setApplication(application);
        environnementIntegration.setHomePath("/home/app1");
        environnementIntegration.setNorme(norme);
        environnementIntegration.setName("testEnv");

        RetourBatch retourBatch = new RetourBatch();
        retourBatch.setReturnCode(5);


        LogBatch logBatchExpected = new LogBatch();
        logBatchExpected.setId(1L);
        when(environnementService.getDateTraiement(
                eq(environnementIntegration.getId()))
        ).thenReturn(simpleDateFormat.parse("01/02/2018"));

        when(shellService.runCommandNew(
                eq(environnementIntegration), eq(commandExpected))
        ).thenReturn(retourBatch);


        when(logBatchRepository.save(
                argThat(logBatch -> {
                            try {
                                return logBatch.getEnvironnement().getId().equals(environnementIntegration.getId())
                                        && logBatch.getBatch().getId().equals(batch.getId())
                                        && logBatch.getDateTraitement().equals(simpleDateFormat.parse("01/02/2018"))
                                        && logBatch.getUser().getId().equals(user.getId())
                                        && logBatch.getReturnCode() == retourBatch.getReturnCode();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }
                )
        )).thenReturn(logBatchExpected);

        doNothing().when(notificationService).createNotification(any(), any(), eq(true));

        when(batchRepository.getOne(eq(batch.getId()))).thenReturn(batch);
        when(environnementService.getEnvironnement(eq(environnementIntegration.getId()))).thenReturn(environnementIntegration);

        when(normeService.getShellPath(eq(norme), eq("AA1"))).thenReturn(norme.getPathShell());

        when(userService.getUser("user")).thenReturn(Optional.of(user));
        batchService.jobRunBatch(batch.getId(), environnementIntegration.getId(), batch.getParams(), "user");

        verify(environnementService, times(1)).getDateTraiement(
                argThat(environnement -> environnementIntegration.getId().equals(environnement))
        );

        verify(shellService, times(1)).runCommandNew(
                argThat(environnement -> environnement.getId().equals(environnementIntegration.getId())
                ), eq(commandExpected)
        );

        verify(userService, times(0)).getUserWithAuthorities();
        verify(logBatchRepository, times(1)).save(
                argThat(logBatch -> {
                    try {
                        return logBatch.getEnvironnement().getId().equals(environnementIntegration.getId())
                                && logBatch.getBatch().getId().equals(batch.getId())
                                && logBatch.getDateTraitement().equals(simpleDateFormat.parse("01/02/2018"))
                                && logBatch.getUser().getId().equals(user.getId())
                                && logBatch.getReturnCode() == retourBatch.getReturnCode();
                    } catch (ParseException e) {
                        e.printStackTrace();
                            }
                            return false;
                        }
                )
        );

        verify(notificationService, times(1)).createNotification("[AA1] Le batch testName sur l'environnement testEnv vient de se terminer avec le code retour 5", user, true);
    }


    @Test
    public void removeBatchsWithoutEnvironnement() {
        List<Batch> batchList = new ArrayList<>();
        Batch batch1 = new Batch();
        batch1.setId(1L);
        batchList.add(batch1);

        Batch batch2 = new Batch();
        batch2.setId(2L);
        batchList.add(batch2);

        List<LogBatch> logBatches1 = new ArrayList<>();
        LogBatch logBatch1 = new LogBatch();
        logBatch1.setId(1L);
        logBatches1.add(logBatch1);

        List<LogBatch> logBatches2 = new ArrayList<>();

        when(batchRepository.findBatchWithoutEnvironnement()).thenReturn(batchList);

        List<Scheduling> schedulings = new ArrayList<>();
        Scheduling scheduling = Scheduling.builder().id(10L).build();
        schedulings.add(scheduling);
        when(schedulingRepository.findAllByBatchId(1L)).thenReturn(schedulings);
        when(schedulingRepository.findAllByBatchId(2L)).thenReturn(new ArrayList<>());

        batchService.removeBatchsWithoutEnvironnement();

        verify(logBatchRepository).deleteAllByBatchId(eq(1L));
        verify(logBatchRepository).deleteAllByBatchId(eq(2L));

        verify(jobScheduler, times(1)).delete(eq("10"));
        verify(schedulingRepository, times(1)).delete(eq(scheduling));

        verify(batchRepository).delete(batch1);
        verify(batchRepository).delete(batch2);

    }

    @Test
    public void runBatchWithoutParams() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        String commandExpected = "/home/app1/shell/Itest.ksh 20180201 toto ";

        User user = new User();
        user.setId(1L);


        Batch batch = new Batch();
        batch.setPath("test.ksh");
        batch.setName("testName");
        batch.setId(1L);
        batch.setParams("${DATE_TRAITEMENT} toto");

        Norme norme = new Norme();
        norme.setName("norme 1");
        norme.setPathShell("shell/");
        norme.setCommandLine("/bin/bash $1");

        Application application = new Application();
        application.setId(1L);
        application.setDateParametrePattern("yyyyMMdd");
        application.setCode("AA1");

        Environnement environnementIntegration = new Environnement();
        environnementIntegration.setId(1L);
        environnementIntegration.setPrefix("I");
        environnementIntegration.setApplication(application);
        environnementIntegration.setHomePath("/home/app1");
        environnementIntegration.setNorme(norme);
        environnementIntegration.setName("testEnv");

        RetourBatch retourBatch = new RetourBatch();
        retourBatch.setReturnCode(5);


        LogBatch logBatchExpected = new LogBatch();
        logBatchExpected.setId(1L);
        when(environnementService.getDateTraiement(
                eq(environnementIntegration.getId()))
        ).thenReturn(simpleDateFormat.parse("01/02/2018"));

        when(shellService.runCommandNew(
                eq(environnementIntegration), eq(commandExpected))
        ).thenReturn(retourBatch);

        when(logBatchRepository.save(
                argThat(logBatch -> {
                            try {
                                return logBatch.getEnvironnement().getId().equals(environnementIntegration.getId())
                                        && logBatch.getBatch().getId().equals(batch.getId())
                                        && logBatch.getDateTraitement().equals(simpleDateFormat.parse("01/02/2018"))
                                        && logBatch.getUser().getId().equals(user.getId())
                                        && logBatch.getReturnCode() == retourBatch.getReturnCode();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }
                )
        )).thenReturn(logBatchExpected);

        doNothing().when(notificationService).createNotification(any(), any(), eq(true));
        when(normeService.getShellPath(eq(norme), eq("AA1"))).thenReturn(norme.getPathShell());
        when(batchRepository.getOne(batch.getId())).thenReturn(batch);
        when(userService.getUser("user")).thenReturn(Optional.of(user));
        when(environnementService.getEnvironnement(environnementIntegration.getId())).thenReturn(environnementIntegration);
        batchService.jobRunBatch(batch.getId(), environnementIntegration.getId(), "user");

        verify(environnementService, times(1)).getDateTraiement(
                argThat(environnement -> environnementIntegration.getId().equals(environnement))
        );

        verify(shellService, times(1)).runCommandNew(
                argThat(environnement -> environnement.getId().equals(environnementIntegration.getId())
                ), eq(commandExpected)
        );

        verify(logBatchRepository,times(1)).save(
                argThat(logBatch -> {
                            try {
                                return logBatch.getEnvironnement().getId().equals(environnementIntegration.getId())
                                        && logBatch.getBatch().getId().equals(batch.getId())
                                        && logBatch.getDateTraitement().equals(simpleDateFormat.parse("01/02/2018"))
                                        && logBatch.getUser().getId().equals(user.getId())
                                        && logBatch.getReturnCode() == retourBatch.getReturnCode();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }
                )
        );

        verify(notificationService, times(1)).createNotification("[AA1] Le batch testName sur l'environnement testEnv vient de se terminer avec le code retour 5", user, true);
    }

    @Test
    public void testSaveBatch() {
        Batch batch = new Batch();
        batch.setId(1L);

        when(batchRepository.save(eq(batch))).thenReturn(batch);

        Batch result = batchService.saveBatch(batch);

        verify(batchRepository).save(eq(batch));
        assertEquals(batch, result);
    }

    @Test
    public void testGetBatch() {
        Batch batch = new Batch();
        batch.setId(1L);

        when(batchRepository.getOne(eq(1L))).thenReturn(batch);
        Batch result = batchService.getBatch(1L);

        assertEquals(batch, result);
    }

    @Test
    public void testDeleteBatchById() {
        List<Scheduling> schedulings = new ArrayList<>();
        Scheduling scheduling = Scheduling.builder().id(10L).build();
        schedulings.add(scheduling);
        when(schedulingRepository.findAllByBatchId(1L)).thenReturn(schedulings);

        batchService.deleteBatch(1L);
        verify(logBatchRepository).deleteAllByBatchId(1L);
        verify(batchRepository).deleteById(1L);
        verify(jobScheduler, times(1)).delete(eq("10"));
        verify(schedulingRepository, times(1)).delete(eq(scheduling));

    }

    @Test
    public void deleteBatch() {
        Batch batch = new Batch();
        batch.setId(1L);

        List<Scheduling> schedulings = new ArrayList<>();
        Scheduling scheduling = Scheduling.builder().id(10L).build();
        schedulings.add(scheduling);
        when(schedulingRepository.findAllByBatchId(1L)).thenReturn(schedulings);

        batchService.deleteBatch(batch);

        verify(logBatchRepository).deleteAllByBatchId(1L);
        verify(batchRepository).deleteById(1L);
        verify(jobScheduler, times(1)).delete(eq("10"));
        verify(schedulingRepository, times(1)).delete(eq(scheduling));
    }

    @Test
    public void testCurrentJob(){
        batchService.addJob(10L, 12L);
        batchService.addJob(11L, 12L);
        assertEquals((Long)12L, batchService.getCurrentJobForEnv(10L).get(0));
        assertEquals((Long)12L, batchService.getCurrentJobForEnv(10L).get(0));
        batchService.deleteJob(10L, 12L);
        assertEquals(0, batchService.getCurrentJobForEnv(10L).size());

        batchService.addJob(12L, 13L);
        batchService.addJob(12L, 13L);
        assertEquals(2, batchService.getCurrentJobForEnv(12L).size());
        batchService.deleteJob(12L, 13L);
        assertEquals(1, batchService.getCurrentJobForEnv(12L).size());
    }
}
