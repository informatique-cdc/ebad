package fr.icdc.ebad.service.scheduling;

import com.jcraft.jsch.JSchException;
import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.service.BatchService;
import fr.icdc.ebad.service.util.EbadServiceException;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@Scope("prototype")
public class RunnableBatch implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableBatch.class);
    private final BatchService batchService;
    @Setter
    private Scheduling scheduling;

    public RunnableBatch(BatchService batchService) {
        this.batchService = batchService;
    }

    @Override
    public void run() {
        LOGGER.debug("RUN BATCH SCHEDULING - START - " + scheduling.getBatch().getId() + " with parameters " + scheduling.getParameters() + " on env " + scheduling.getEnvironnement().getName());
        try {
            batchService.runBatch(scheduling.getBatch().getId(), scheduling.getEnvironnement().getId(), scheduling.getParameters());
        } catch (JSchException | EbadServiceException | IOException e) {
            LOGGER.error("Error when trying to run batch scheduling", e);
        }
        LOGGER.debug("RUN BATCH SCHEDULING - END - " + scheduling.getBatch().getId() + " with parameters " + scheduling.getParameters() + " on env " + scheduling.getEnvironnement().getName());
    }
}
