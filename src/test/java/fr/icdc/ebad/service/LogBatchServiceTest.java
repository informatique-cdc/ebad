package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.domain.QLogBatch;
import fr.icdc.ebad.repository.LogBatchRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogBatchServiceTest {
    @InjectMocks
    private LogBatchService logBatchService;

    @Mock
    private LogBatchRepository logBatchRepository;

    @Test
    public void getAllLogBatchWithPageable() {
        Predicate predicate = QLogBatch.logBatch.id.eq(1L);
        Pageable pageable = PageRequest.of(0, 20);

        List<LogBatch> logBatches = new ArrayList<>();
        LogBatch logBatch1 = new LogBatch();
        logBatch1.setId(1L);
        LogBatch logBatch2 = new LogBatch();
        logBatch2.setId(2L);

        logBatches.add(logBatch1);
        logBatches.add(logBatch2);

        PageImpl<LogBatch> logBatchPage = new PageImpl<>(logBatches);

        when(logBatchRepository.findAll(eq(predicate), eq(pageable))).thenReturn(logBatchPage);
        Page<LogBatch> results = logBatchService.getAllLogBatchWithPageable(predicate, pageable);

        assertEquals(2, results.getContent().size());
        assertThat(results, hasItem(logBatch1));
        assertThat(results, hasItem(logBatch2));
    }
}
