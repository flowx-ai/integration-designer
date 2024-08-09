package ai.flowx.integration.instances.domain;

public enum TokenNodeStatus {
    ARRIVED, EXECUTING, EXECUTED_COMPLETE, EXECUTED_PARTIAL, WAITING_MESSAGE_EVENT, WAITING_TIMER_EVENT, WAITING_MESSAGE, MESSAGE_RECEIVED, MESSAGE_RESPONSE_TIMED_OUT;

    public boolean isNotExecutedComplete() {
        return this != EXECUTED_COMPLETE;
    }

    public boolean isExecutedComplete() {
        return this == EXECUTED_COMPLETE;
    }
}
