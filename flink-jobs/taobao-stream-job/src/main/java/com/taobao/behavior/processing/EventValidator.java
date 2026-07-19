package com.taobao.behavior.processing;

import com.taobao.behavior.avro.UserBehaviorEvent;
import com.taobao.behavior.model.InvalidBehaviorEvent;
import java.util.Optional;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class EventValidator extends ProcessFunction<UserBehaviorEvent, UserBehaviorEvent> {
    public static final OutputTag<InvalidBehaviorEvent> INVALID_EVENTS =
            new OutputTag<InvalidBehaviorEvent>("invalid-events") {};

    @Override
    public void processElement(
            UserBehaviorEvent event,
            Context context,
            Collector<UserBehaviorEvent> output) {
        Optional<String> invalidReason = invalidReason(event);
        if (invalidReason.isPresent()) {
            context.output(
                    INVALID_EVENTS,
                    new InvalidBehaviorEvent(
                            event.getEventId(), event.getSourceSequence(), invalidReason.get()));
            return;
        }
        output.collect(event);
    }

    public static Optional<String> invalidReason(UserBehaviorEvent event) {
        if (event == null) {
            return Optional.of("event must not be null");
        }
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            return Optional.of("event_id must not be blank");
        }
        if (event.getUserId() <= 0 || event.getItemId() <= 0 || event.getCategoryId() <= 0) {
            return Optional.of("IDs must be positive");
        }
        if (event.getBehaviorType() == null) {
            return Optional.of("behavior_type must not be null");
        }
        if (event.getEventTimeMs() < 0) {
            return Optional.of("event_time_ms must be non-negative");
        }
        if (event.getSourceSequence() < 0) {
            return Optional.of("source_sequence must be non-negative");
        }
        if (event.getReplayRunId() == null || event.getReplayRunId().isBlank()) {
            return Optional.of("replay_run_id must not be blank");
        }
        return Optional.empty();
    }
}
