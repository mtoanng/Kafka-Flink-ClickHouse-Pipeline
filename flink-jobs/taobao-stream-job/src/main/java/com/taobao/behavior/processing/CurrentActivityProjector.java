package com.taobao.behavior.processing;

import com.taobao.behavior.avro.UserBehaviorEvent;
import com.taobao.behavior.model.UserCurrentActivity;
import java.util.Optional;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class CurrentActivityProjector
        extends KeyedProcessFunction<Long, UserBehaviorEvent, UserCurrentActivity> {
    private transient ValueState<UserCurrentActivity> currentActivity;

    @Override
    public void open(Configuration parameters) {
        currentActivity =
                getRuntimeContext()
                        .getState(
                                new ValueStateDescriptor<>(
                                        "current-user-activity", UserCurrentActivity.class));
    }

    @Override
    public void processElement(
            UserBehaviorEvent event,
            Context context,
            Collector<UserCurrentActivity> output)
            throws Exception {
        Optional<UserCurrentActivity> next =
                project(currentActivity.value(), event, context.timerService().currentProcessingTime());
        if (next.isPresent()) {
            currentActivity.update(next.get());
            output.collect(next.get());
        }
    }

    public static Optional<UserCurrentActivity> project(
            UserCurrentActivity previous, UserBehaviorEvent event, long updatedAtMs) {
        if (previous != null && !isNewer(event, previous)) {
            return Optional.empty();
        }
        long eventCount = previous == null ? 1L : previous.getSessionEventCount() + 1L;
        return Optional.of(
                new UserCurrentActivity(
                        event.getUserId(),
                        event.getEventTimeMs(),
                        event.getBehaviorType().toString(),
                        event.getItemId(),
                        event.getCategoryId(),
                        eventCount,
                        updatedAtMs,
                        event.getSourceSequence()));
    }

    private static boolean isNewer(UserBehaviorEvent event, UserCurrentActivity previous) {
        return event.getEventTimeMs() > previous.getLastEventTimeMs()
                || (event.getEventTimeMs() == previous.getLastEventTimeMs()
                        && event.getSourceSequence() > previous.getLastSourceSequence());
    }
}
