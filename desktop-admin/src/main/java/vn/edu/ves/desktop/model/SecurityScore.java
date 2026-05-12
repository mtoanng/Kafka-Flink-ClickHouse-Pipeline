package vn.edu.ves.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Map view <code>v_security_score</code> (07_init_security_features.sql, mục 9).
 *
 * <p>Single-row view: {pillar1_score, pillar2_score, pillar3_score, pillar4_score,
 * overall_score, status, computed_at}.</p>
 */
public class SecurityScore {

    private BigDecimal overallScore;
    private BigDecimal pillar1Score;
    private BigDecimal pillar2Score;
    private BigDecimal pillar3Score;
    private BigDecimal pillar4Score;
    private String status;
    private LocalDateTime computedAt;

    public SecurityScore() {
    }

    public BigDecimal getOverallScore() { return overallScore; }
    public void setOverallScore(BigDecimal overallScore) { this.overallScore = overallScore; }

    public BigDecimal getPillar1Score() { return pillar1Score; }
    public void setPillar1Score(BigDecimal pillar1Score) { this.pillar1Score = pillar1Score; }

    public BigDecimal getPillar2Score() { return pillar2Score; }
    public void setPillar2Score(BigDecimal pillar2Score) { this.pillar2Score = pillar2Score; }

    public BigDecimal getPillar3Score() { return pillar3Score; }
    public void setPillar3Score(BigDecimal pillar3Score) { this.pillar3Score = pillar3Score; }

    public BigDecimal getPillar4Score() { return pillar4Score; }
    public void setPillar4Score(BigDecimal pillar4Score) { this.pillar4Score = pillar4Score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }

    /** Tiện cho UI: 0.0 - 1.0 cho ProgressIndicator. */
    public double getOverallScoreRatio() {
        if (overallScore == null) return 0.0;
        return Math.min(1.0, Math.max(0.0, overallScore.doubleValue() / 100.0));
    }

    @Override
    public String toString() {
        return "SecurityScore{overall=" + overallScore + ", status=" + status + '}';
    }
}
