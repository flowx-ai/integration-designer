package ai.flowx.integration.advancing.worker;

import ai.flowx.integration.advancing.config.AdvancingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdvancingWorkerGroupController implements Runnable {

    private static final long PAUSE_DURATION = TimeUnit.SECONDS.toNanos(1);

    private final AdvancingWorkerGroup workerGroup;
    private final AdvancingProperties advancingProperties;
    private LocalDateTime workQueuedLastTimestamp;

    @PostConstruct
    public void init() {
        log.info("Initialized worker group controller thread !");

        if (workerGroup.hasWorkers()) {
            workerGroup.cooldown();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startThread() {
        new Thread(this).start();
        log.info("Started worker group controller thread !");
    }

    @Override
    public void run() {
        int maxAdvancingThreads = advancingProperties.getThreads();

        while (true) {

            try {
                boolean thereIsWorkQueued = workerGroup.isAdvancingWorkQueued();
                if (thereIsWorkQueued) {
                    workQueuedLastTimestamp = LocalDateTime.now();

                    if (workerGroup.getNoOfWorkers() < maxAdvancingThreads) {
                        workerGroup.rampup();
                    }
                } else {
                    if (workQueuedLastTimestamp != null) {
                        long quietSeconds = Duration.between(workQueuedLastTimestamp, LocalDateTime.now()).getSeconds();

                        if (quietSeconds >= advancingProperties.getCooldownAfterSeconds() && workerGroup.getNoOfWorkers() >= maxAdvancingThreads) {
                            workerGroup.cooldown();
                        }
                    }
                }
            } catch (Exception e) {
                if (isConnectionClosed(e)) {
                    log.error("Database connection is no longer available !");
                    workerGroup.cleanup();
                    init();
                    continue;
                }

                log.error("Exception in workgroup controller: " + e.getMessage(), e);
            }
            LockSupport.parkNanos(PAUSE_DURATION);
        }

    }

    private boolean isConnectionClosed(Exception e) {
        return e.getMessage().startsWith("Closed Statement")
                || e.getMessage().startsWith("This connection has been closed")
                || e.getMessage().startsWith("Closed Connection")
                || e.getMessage().startsWith("Could not obtain connection because");
    }

}
