package fr.icdc.ebad.web.rest;


import fr.icdc.ebad.service.scheduling.RunnableBatch;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/scheduling")
@Tag(name = "Scheduling", description = "the scheduling API")
public class SchedulingResource {
    private final ThreadPoolTaskScheduler taskScheduler;

    public SchedulingResource(ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @GetMapping
    public void todo() {
        taskScheduler.schedule(
                new RunnableBatch("Specific time, 3 Seconds from now"),
                new Date(System.currentTimeMillis() + 3000)
        );
    }
}
