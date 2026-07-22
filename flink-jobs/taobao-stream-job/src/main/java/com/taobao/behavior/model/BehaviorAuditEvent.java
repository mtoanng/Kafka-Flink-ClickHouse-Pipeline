package com.taobao.behavior.model;

import com.taobao.behavior.avro.UserBehaviorEvent;
import java.io.Serializable;

/** Durable diagnostic representation for decoded invalid and late events. */
public class BehaviorAuditEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String replayRunId;
    private long userId;
    private long itemId;
    private long categoryId;
    private long eventTimeMs;
    private long sourceSequence;
    private String reasonCode;
    private String reasonMessage;

    public BehaviorAuditEvent() {}

    public BehaviorAuditEvent(
            String eventId,
            String replayRunId,
            long userId,
            long itemId,
            long categoryId,
            long eventTimeMs,
            long sourceSequence,
            String reasonCode,
            String reasonMessage) {
        this.eventId = eventId;
        this.replayRunId = replayRunId;
        this.userId = userId;
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.eventTimeMs = eventTimeMs;
        this.sourceSequence = sourceSequence;
        this.reasonCode = reasonCode;
        this.reasonMessage = reasonMessage;
    }

    public static BehaviorAuditEvent fromEvent(
            UserBehaviorEvent event, String reasonCode, String reasonMessage) {
        if (event == null) {
            return new BehaviorAuditEvent(
                    "missing-event-id",
                    "missing-replay-run",
                    0L,
                    0L,
                    0L,
                    0L,
                    -1L,
                    reasonCode,
                    reasonMessage);
        }
        return new BehaviorAuditEvent(
                event.getEventId() == null ? "missing-event-id" : event.getEventId().toString(),
                event.getReplayRunId() == null ? "missing-replay-run" : event.getReplayRunId().toString(),
                event.getUserId(),
                event.getItemId(),
                event.getCategoryId(),
                event.getEventTimeMs(),
                event.getSourceSequence(),
                reasonCode,
                reasonMessage);
    }

    public String getEventId() { return eventId; }
    public String getReplayRunId() { return replayRunId; }
    public long getUserId() { return userId; }
    public long getItemId() { return itemId; }
    public long getCategoryId() { return categoryId; }
    public long getEventTimeMs() { return eventTimeMs; }
    public long getSourceSequence() { return sourceSequence; }
    public String getReasonCode() { return reasonCode; }
    public String getReasonMessage() { return reasonMessage; }

    @Override
    public String toString() {
        return "BehaviorAuditEvent{eventId='" + eventId + "', replayRunId='" + replayRunId
                + "', sourceSequence=" + sourceSequence + ", reasonCode='" + reasonCode + "'}";
    }
}
