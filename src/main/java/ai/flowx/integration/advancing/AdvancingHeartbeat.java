package ai.flowx.integration.advancing;

import ai.flowx.integration.advancing.utils.HostUtils;
import ai.flowx.integration.advancing.repository.AdvancingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@RequiredArgsConstructor
@Component
public class AdvancingHeartbeat {
    private final AdvancingRepository advancingRepo;
    private String currentPodName;

    @PostConstruct
    public void init() {
        this.currentPodName = HostUtils.getCurrentHostname();
    }

    @Scheduled(cron = "${advancing.scheduler.heartbeat.cronExpression}")
    public void heartbeat() {
        advancingRepo.updateHeartbeat(currentPodName);
    }
}
