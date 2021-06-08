package fr.icdc.ebad.service;

import org.jobrunr.server.BackgroundJobServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class JobRunrService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunrService.class);
    private final BackgroundJobServer backgroundJobServerk;

    public JobRunrService(BackgroundJobServer backgroundJobServerk) {
        this.backgroundJobServerk = backgroundJobServerk;
    }

    @Scheduled(fixedDelay = 1000 * 60)
    public void aliveCheck() {
        LOGGER.info("Alive check...");
        var isRunning = backgroundJobServerk.isRunning();
        if (!isRunning) {
            LOGGER.warn("backgroundJobServer is not running...");
            restartJobRunr();
        }
    }

    public void restartJobRunr(){
        LOGGER.info("Starting backgroundJobServer again...");
        backgroundJobServerk.start();
    }
}
