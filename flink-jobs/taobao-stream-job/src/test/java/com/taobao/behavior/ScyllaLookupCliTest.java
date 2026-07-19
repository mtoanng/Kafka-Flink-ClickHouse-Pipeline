package com.taobao.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ScyllaLookupCliTest {
    @Test
    void requiresAPositiveFixedUserId() {
        assertEquals(100L, ScyllaLookupCli.parseUserId("100"));
        assertThrows(IllegalArgumentException.class, () -> ScyllaLookupCli.parseUserId("0"));
        assertThrows(NumberFormatException.class, () -> ScyllaLookupCli.parseUserId("not-a-number"));
    }
}
