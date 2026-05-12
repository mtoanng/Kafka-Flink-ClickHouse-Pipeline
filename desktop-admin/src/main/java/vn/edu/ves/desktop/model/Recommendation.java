package vn.edu.ves.desktop.model;

import java.time.LocalDateTime;

/**
 * Row của <code>v_active_recommendations</code>.
 *
 * <p>Columns: id, pillar, action_type, severity, title, message, suggested_data,
 * suggested_at, age_seconds, expires_at, is_expired.</p>
 */
public class Recommendation {

    private long id;
    private int pillar;
    private String actionType;
    private String severity;
    private String title;
    private String message;
    private String suggestedDataJson;
    private LocalDateTime suggestedAt;
    private int ageSeconds;
    private LocalDateTime expiresAt;
    private boolean expired;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getPillar() { return pillar; }
    public void setPillar(int pillar) { this.pillar = pillar; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSuggestedDataJson() { return suggestedDataJson; }
    public void setSuggestedDataJson(String suggestedDataJson) { this.suggestedDataJson = suggestedDataJson; }

    public LocalDateTime getSuggestedAt() { return suggestedAt; }
    public void setSuggestedAt(LocalDateTime suggestedAt) { this.suggestedAt = suggestedAt; }

    public int getAgeSeconds() { return ageSeconds; }
    public void setAgeSeconds(int ageSeconds) { this.ageSeconds = ageSeconds; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
}
