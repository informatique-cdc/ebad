package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        when(shellService.runCommand(
                eq(environnementIntegration), eq(commandExpected))
        ).thenReturn(retourBatch);

        when(userService.getUserWithAuthorities()).thenReturn(user);

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

        doNothing().when(notificationService).createNotificationForCurrentUser(any());
        when(normeService.getShellPath(eq(norme), eq("AA1"))).thenReturn(norme.getPathShell());
        batchService.runBatch(batch,environnementIntegration);

        verify(environnementService,times(1)).getDateTraiement(
                argThat(environnement -> environnementIntegration.getId().equals(environnement))
        );

        verify(shellService,times(1)).runCommand(
                argThat(environnement -> environnement.getId().equals(environnementIntegration.getId())
                ), eq(commandExpected)
        );

        verify(userService,times(1)).getUserWithAuthorities();
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

        verify(notificationService, times(1)).createNotificationForCurrentUser(eq("[AA1] Le batch testName sur l'environnement testEnv vient de se terminer avec le code retour 5"));
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

        when(shellService.runCommand(
                eq(environnementIntegration), eq(commandExpected))
        ).thenReturn(retourBatch);

        when(userService.getUserWithAuthorities()).thenReturn(user);

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

        doNothing().when(notificationService).createNotificationForCurrentUser(any());

        when(batchRepository.getOne(eq(batch.getId()))).thenReturn(batch);
        when(environnementService.getEnvironnement(eq(environnementIntegration.getId()))).thenReturn(environnementIntegration);

        when(normeService.getShellPath(eq(norme), eq("AA1"))).thenReturn(norme.getPathShell());

        batchService.runBatch(batch.getId(), environnementIntegration.getId(), batch.getParams());

        verify(environnementService, times(1)).getDateTraiement(
                argThat(environnement -> environnementIntegration.getId().equals(environnement))
        );

        verify(shellService, times(1)).runCommand(
                argThat(environnement -> environnement.getId().equals(environnementIntegration.getId())
                ), eq(commandExpected)
        );

        verify(userService, times(1)).getUserWithAuthorities();
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

        verify(notificationService, times(1)).createNotificationForCurrentUser(eq("[AA1] Le batch testName sur l'environnement testEnv vient de se terminer avec le code retour 5"));
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

        batchService.removeBatchsWithoutEnvironnement();

        verify(logBatchRepository).deleteAllByBatchId(eq(1L));
        verify(logBatchRepository).deleteAllByBatchId(eq(2L));

        verify(batchRepository).delete(batch1);
        verify(batchRepository).delete(batch2);

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
        batchService.deleteBatch(1L);
        verify(logBatchRepository).deleteAllByBatchId(eq(1L));
        verify(batchRepository).deleteById(eq(1L));
    }

    @Test
    public void deleteBatch() {
        Batch batch = new Batch();
        batch.setId(1L);

        batchService.deleteBatch(batch);

        verify(logBatchRepository).deleteAllByBatchId(eq(1L));
        verify(batchRepository).delete(eq(batch));
    }
}
