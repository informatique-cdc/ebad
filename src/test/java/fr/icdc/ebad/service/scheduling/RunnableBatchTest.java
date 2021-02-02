package fr.icdc.ebad.service.scheduling;

import com.jcraft.jsch.JSchException;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.service.BatchService;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RunnableBatchTest {

    @Test
    public void run() throws EbadServiceException, JSchException, IOException {
        BatchService batchService = mock(BatchService.class);
        RunnableBatch runnableBatch = new RunnableBatch(batchService);

        Batch batch = Batch.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();

        Scheduling scheduling = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .environnement(environnement)
                .parameters("test")
                .cron("10 * * * * ?")
                .build();

        runnableBatch.setScheduling(scheduling);
        runnableBatch.run();
        verify(batchService).runBatch(eq(batch.getId()), eq(environnement.getId()), eq("test"));
    }
}
