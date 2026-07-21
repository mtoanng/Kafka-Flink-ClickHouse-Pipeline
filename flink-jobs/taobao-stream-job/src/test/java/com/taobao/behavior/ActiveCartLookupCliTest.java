package com.taobao.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ActiveCartLookupCliTest {
    @Test
    void supportsOnlyTheFixedActiveCartLookup() {
        assertEquals(100L, ActiveCartLookupCli.parseUserId(new String[] {"--user-id", "100"}));
        assertThrows(IllegalArgumentException.class,
                () -> ActiveCartLookupCli.parseUserId(new String[] {"100"}));
        assertThrows(IllegalArgumentException.class,
                () -> ActiveCartLookupCli.parseUserId(new String[] {"--user-id", "0"}));
        assertEquals(
                "SELECT user_id, item_id, category_id, added_at, last_updated_at FROM taobao_streaming.user_active_cart WHERE user_id = ?",
                ActiveCartLookupCli.selectCql("taobao_streaming", "user_active_cart"));
    }
}
