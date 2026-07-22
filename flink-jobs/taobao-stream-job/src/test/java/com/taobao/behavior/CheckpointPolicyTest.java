package com.taobao.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taobao.behavior.processing.CheckpointPolicy;
import org.junit.jupiter.api.Test;

class CheckpointPolicyTest {
    @Test
    void checkpointingCanBeDisabledForCredentialIndependentChecks() {
        CheckpointPolicy policy = CheckpointPolicy.fromValues("false", "60000", "");

        assertFalse(policy.isEnabled());
        assertEquals(60_000L, policy.getIntervalMs());
    }

    @Test
    void enabledCheckpointingRequiresDurableStoragePath() {
        CheckpointPolicy policy = CheckpointPolicy.fromValues("true", "10000", "/var/lib/flink/checkpoints");

        assertTrue(policy.isEnabled());
        assertEquals("/var/lib/flink/checkpoints", policy.getStoragePath());
        assertThrows(
                IllegalArgumentException.class,
                () -> CheckpointPolicy.fromValues("true", "10000", ""));
    }

    @Test
    void checkpointIntervalMustBeSane() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CheckpointPolicy.fromValues("sometimes", "60000", ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> CheckpointPolicy.fromValues("false", "not-a-number", ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> CheckpointPolicy.fromValues("false", "999", ""));
    }
}
