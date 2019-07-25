package fr.icdc.ebad.service;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.StatistiquesDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class StatistiquesServiceTest {
    @MockBean
    private ApplicationRepository applicationRepository;
    @MockBean
    private BatchRepository batchRepository;
    @MockBean
    private LogBatchRepository logBatchRepository;
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private StatistiquesService statistiquesService;

    @Test
    public void generationStatistiques() {
        List<Long> countBatchByDay = new ArrayList<>();
        countBatchByDay.add(5L);
        when(batchRepository.count()).thenReturn(1L);
        when(logBatchRepository.count()).thenReturn(2L);
        when(logBatchRepository.avgTime()).thenReturn(3L);
        when(userRepository.count()).thenReturn(4L);
        when(logBatchRepository.countBatchByDay(any())).thenReturn(countBatchByDay);
        when(applicationRepository.count()).thenReturn(6L);

        StatistiquesDto statistiquesDto = statistiquesService.generationStatistiques();

        assertEquals(1L, statistiquesDto.getNbrBatchs(), 0);
        assertEquals(2L, statistiquesDto.getNbrBatchsLances(), 0);
        assertEquals(3L, statistiquesDto.getTpsMoyenBatch(), 0);
        assertEquals(4L, statistiquesDto.getNbrUtilisateurs(), 0);
        assertEquals(6L, statistiquesDto.getNbrApplications(), 0);
        assertEquals(1, statistiquesDto.getNbrBatchLancesParJour().size());
        assertEquals(5L, statistiquesDto.getNbrBatchLancesParJour().get(0), 0);
    }
}
