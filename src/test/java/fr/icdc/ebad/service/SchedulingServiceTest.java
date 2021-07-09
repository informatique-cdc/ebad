package fr.icdc.ebad.service;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.EnvironnementRepository;
import fr.icdc.ebad.repository.SchedulingRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class SchedulingServiceTest {
    @Autowired
    private SchedulingService schedulingService;

    @MockBean
    private SchedulingRepository schedulingRepository;

    @MockBean
    private BatchRepository batchRepository;

    @MockBean
    private EnvironnementRepository environnementRepository;

    @MockBean
    private JobScheduler jobScheduler;

    @Test
    public void listByEnvironment() {
        Scheduling scheduling1 = Scheduling.builder().id(1L).build();
        Scheduling scheduling2 = Scheduling.builder().id(1L).build();
        List<Scheduling> schedulings = new ArrayList<>();
        schedulings.add(scheduling1);
        schedulings.add(scheduling2);
        PageImpl<Scheduling> page = new PageImpl(schedulings);

        when(schedulingRepository.findAllByEnvironnementId(eq(1L), any())).thenReturn(page);
        Page<Scheduling> result = schedulingService.listByEnvironment(1L, Pageable.unpaged());

        assertEquals(page, result);
    }

    @Test
    public void listAll() {
        Scheduling scheduling1 = Scheduling.builder().id(1L).build();
        Scheduling scheduling2 = Scheduling.builder().id(1L).build();
        List<Scheduling> schedulings = new ArrayList<>();
        schedulings.add(scheduling1);
        schedulings.add(scheduling2);
        PageImpl<Scheduling> page = new PageImpl(schedulings);

        when(schedulingRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<Scheduling> result = schedulingService.listAll(Pageable.unpaged());

        assertEquals(page, result);
    }

    @Test
    public void saveAndRun() throws EbadServiceException {
        Batch batch = Batch.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();
        String parameters = "testParams";
        String cron = "10 * * * * *";

        Scheduling scheduling = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .environnement(environnement)
                .parameters(parameters)
                .cron(cron)
                .build();

        when(batchRepository.getById(1L)).thenReturn(batch);
        when(environnementRepository.getById(2L)).thenReturn(environnement);
        when(schedulingRepository.save(any())).thenReturn(scheduling);
        Scheduling result = schedulingService.saveAndRun(1L, 2L, parameters, cron);

        assertEquals(scheduling, result);

        ArgumentCaptor<Scheduling> argument = ArgumentCaptor.forClass(Scheduling.class);
        verify(schedulingRepository, times(1)).save(argument.capture());
        assertEquals(scheduling.getBatch(), argument.getValue().getBatch());
        assertEquals(scheduling.getEnvironnement(), argument.getValue().getEnvironnement());
        assertNull(argument.getValue().getId());
        assertEquals(scheduling.getCron(), argument.getValue().getCron());
        assertEquals(scheduling.getParameters(), argument.getValue().getParameters());
    }

    @Test
    public void remove() throws EbadServiceException {
        Batch batch = Batch.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();
        String parameters = "testParams";
        String cron = "10 * * * * *";

        Scheduling scheduling = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .environnement(environnement)
                .parameters(parameters)
                .cron(cron)
                .build();

        schedulingService.run(scheduling);

        when(schedulingRepository.getById(3L)).thenReturn(scheduling);

        schedulingService.remove(3L);

        verify(schedulingRepository).delete(scheduling);
    }

    @Test
    public void run() throws EbadServiceException {
        Batch batch = Batch.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();
        String parameters = "testParams";
        String cron = "10 * * * * *";

        Scheduling scheduling = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .environnement(environnement)
                .parameters(parameters)
                .cron(cron)
                .build();

        when(jobScheduler.scheduleRecurrently(anyString(), eq(cron), any(JobLambda.class))).thenReturn("myId");
        schedulingService.run(scheduling);
        verify(jobScheduler).scheduleRecurrently(anyString(), eq(cron), any(JobLambda.class));


    }

    @Test(expected = EbadServiceException.class)
    public void runError1() throws EbadServiceException {
        Environnement environnement = Environnement.builder().id(2L).build();
        String parameters = "testParams";
        String cron = "10 * * * * *";

        Scheduling scheduling = Scheduling.builder()
                .id(3L)
                .environnement(environnement)
                .parameters(parameters)
                .cron(cron)
                .build();

        schedulingService.run(scheduling);
    }

    @Test(expected = EbadServiceException.class)
    public void runError2() throws EbadServiceException {
        Batch batch = Batch.builder().id(1L).build();
        String parameters = "testParams";
        String cron = "10 * * * * *";

        Scheduling scheduling = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .parameters(parameters)
                .cron(cron)
                .build();

        schedulingService.run(scheduling);
    }

    @Test
    public void get() {
        Scheduling scheduling = Scheduling.builder().id(1L).build();
        when(schedulingRepository.getById(1L)).thenReturn(scheduling);
        Scheduling result = schedulingService.get(1L);
        assertEquals(scheduling, result);
    }
}
