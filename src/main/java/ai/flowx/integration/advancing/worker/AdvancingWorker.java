package ai.flowx.integration.advancing.worker;

import ai.flowx.integration.advancing.dto.AdvancingEventDTO;
import ai.flowx.integration.advancing.worker.repository.WorkerRepository;
import ai.flowx.integration.advancing.worker.repository.WorkerRepositoryFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

@Slf4j
public class AdvancingWorker implements Runnable {

    private final AdvancingWorkerConfiguration advancingWorkerConfiguration;
    private final WorkerRepository workerRepo;
    private int threadIndex;
    private volatile boolean alive;


    public AdvancingWorker(AdvancingWorkerConfiguration advancingWorkerConfiguration, int threadIndex) {
        this.advancingWorkerConfiguration = advancingWorkerConfiguration;
        this.workerRepo = WorkerRepositoryFactory.createWorkerRepositoryInstance(advancingWorkerConfiguration.getDbType(),
                advancingWorkerConfiguration.getAdvancingProperties(), advancingWorkerConfiguration.getObjectMapper());
        this.threadIndex = threadIndex;
        this.alive = true;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info("Thread started working ....");

        try {
            while (alive) {
                List<Integer> pickedPartitionIds = workerRepo.pickPartition(advancingWorkerConfiguration.getCurrentPodName(), threadIndex);

                if (pickedPartitionIds.isEmpty()) {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(advancingWorkerConfiguration.getAdvancingProperties().getPickingPauseMillis()));
                    continue;
                }
                log.debug("Thread picked partition " + pickedPartitionIds + " !");

                List<Integer> pickedEventIds = workerRepo.pickEventToProcess(pickedPartitionIds);
                if (pickedEventIds.isEmpty()) {
                    log.warn("BUG: Thread didn't picked any event from partition " + pickedPartitionIds + " who was picked from queue ! " +
                            "This means that for each of these partitions, the events counter of the partition is out of sync with the number of events of the partition ! " +
                            "The events counter will be reset to 0 for these partitions if the count of advancing events(NEW,STARTED) remains 0 before the reset !");
                    workerRepo.resetPartitionsCounter(pickedPartitionIds);
                    continue;
                }
                log.debug("Thread picked events " + pickedEventIds + " from partition " + pickedPartitionIds + " !");

                List<AdvancingEventDTO> pickedEvents = workerRepo.loadAdvancingEvents(pickedEventIds);
                for (AdvancingEventDTO pickedEvent : pickedEvents) {
                    try {
                        processEvent(pickedEvent);
                    } catch (Exception e) {
                        log.error("Thread could not process event " + pickedEvent.getId() + " because of exception " + e.getMessage(), e);
                    }
                }
            }
        } finally {
            workerRepo.unpickAllPartitions(advancingWorkerConfiguration.getCurrentPodName(), threadIndex);
            workerRepo.cleanup();
            advancingWorkerConfiguration.getRemoveWorkerFunction().accept(this);
            log.info("AdvancingWorker-" + threadIndex + " finished thread cleanup gracefully ! ");
        }

        log.info("Thread finished the work gracefully ! ");
    }

    private void processEvent(AdvancingEventDTO advancingEvent) throws SQLException {
        int eventId = advancingEvent.getId();
        log.debug("Thread started processing event " + eventId + " ...");

        workerRepo.updateEventProcessingStarted(eventId);
        try {
            advancingWorkerConfiguration.getProcessAdvancingEventFunction().accept(advancingEvent.getAdvanceTokenNotificationDTO());
            workerRepo.updateEventProcessingFinished(eventId);
        } catch (Exception e) {
            String eventProcessingErrorMessage = "Exception: " + e.getMessage() + ". Stacktrace : " + Arrays.stream(e.getStackTrace()).collect(Collectors.toList())
                    + (e.getCause() == null ? "" : "\\n\\n cause:" + e.getCause() + ", cause stack trace: " + Arrays.stream(e.getCause().getStackTrace()).collect(Collectors.toList()));
            log.error("Processing of event with " + eventId + " FAILED ! Error message: " + eventProcessingErrorMessage, e);
            workerRepo.updateEventProcessingFailed(eventId, eventProcessingErrorMessage);
        }
        log.debug("Processed event " + eventId + " !");
    }

    public void die() {
        this.alive = false;
    }

    public int getThreadIndex() {
        return threadIndex;
    }
}
