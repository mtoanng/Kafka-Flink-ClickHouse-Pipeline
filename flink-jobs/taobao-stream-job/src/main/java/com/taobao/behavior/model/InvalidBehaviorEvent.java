package com.taobao.behavior.model;

import java.io.Serializable;

public class InvalidBehaviorEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private long sourceSequence;
    private String reason;

    public InvalidBehaviorEvent() {}

    public InvalidBehaviorEvent(String eventId, long sourceSequence, String reason) {
        this.eventId = eventId;
        this.sourceSequence = sourceSequence;
        this.reason = reason;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getSourceSequence() {
        return sourceSequence;
    }

    public void setSourceSequence(long sourceSequence) {
        this.sourceSequence = sourceSequence;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "InvalidBehaviorEvent{eventId='" + eventId + "', sourceSequence="
                + sourceSequence + ", reason='" + reason + "'}";
    }
}
