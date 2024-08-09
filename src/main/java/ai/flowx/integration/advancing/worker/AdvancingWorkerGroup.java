package ai.flowx.integration.advancing.worker;

import ai.flowx.integration.advancing.config.AdvancingProperties;
import ai.flowx.integration.advancing.utils.HostUtils;
import ai.flowx.integration.advancing.worker.repository.WorkerRepository;
import ai.flowx.integration.advancing.worker.repository.WorkerRepositoryFactory;
import ai.flowx.integration.instances.AdvanceTokenService;
import ai.flowx.integration.advancing.dto.AdvanceTokenNotificationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



@Slf4j
@Component
@RequiredArgsConstructor
public class AdvancingWorkerGroup {

    private final AdvanceTokenService advanceTokenService;
    private final AdvancingProperties advancingProperties;
    private final ObjectMapper objectMapper;

    private int threadIndexSequence = 0;
    private WorkerRepository workerRepo;
    private AdvancingWorkerConfiguration advancingWorkerConfiguration;
    private List<AdvancingWorker> workers = new ArrayList<>();

    @Value("${spring.jpa.database}")
    private String dbType;

    @PostConstruct
    public void init() {
        advancingWorkerConfiguration = AdvancingWorkerConfiguration.builder()
                .currentPodName(HostUtils.getCurrentHostname())
                .dbType(dbType)
                .advancingProperties(advancingProperties)
                .removeWorkerFunction(this::removeWorker)
                .processAdvancingEventFunction(this::processAdvancingEvent)
                .objectMapper(objectMapper)
                .build();
        this.workerRepo = WorkerRepositoryFactory.createWorkerRepositoryInstance(dbType, advancingProperties, objectMapper);
    }

    public void processAdvancingEvent(AdvanceTokenNotificationDTO advancingEvent) {

        Stopwatch timer = Stopwatch.createStarted();

        advanceTokenService.advance(advancingEvent);

        log.debug("Processing advance message took: {} for instance: {}", timer.stop(), advancingEvent.getWInstanceUuid());
    }

    public int nextThreadIndex() {
        return threadIndexSequence++;
    }


    public int getNoOfWorkers() {
        return workers.size();
    }

    public synchronized void removeWorker(AdvancingWorker worker) {
        workers.remove(worker);
        log.debug("Removed AdvancingWorker-" + worker.getThreadIndex() + " from workers list.");
    }

    public void rampup() {
        int rampupWorkers = advancingProperties.getThreads() - workers.size();
        log.info("Ramping up " + rampupWorkers + " advancing workers.... ");

        for (int counter = 1; counter <= rampupWorkers; counter++) {
            int threadIndex = nextThreadIndex();
            AdvancingWorker worker = new AdvancingWorker(advancingWorkerConfiguration, threadIndex);
            workers.add(worker);
            Thread thread = new Thread(worker, "AdvancingWorker-" + threadIndex);
            thread.start();
        }
        log.info("Initialized " + rampupWorkers + " advancing workers !");
    }

    public void cooldown() {
        log.info("Cooling down the advancing workers ... ");
        workers.forEach(worker -> worker.die());
        workers.clear();
        log.info("Advancing workers cooled down !");
    }

    public boolean isAdvancingWorkQueued() throws SQLException {
        return workerRepo.isAdvancingWorkQueued();
    }

    public void cleanup() {
        workerRepo.cleanup();
    }

    public boolean hasWorkers() {
        return !CollectionUtils.isEmpty(workers);
    }
}
