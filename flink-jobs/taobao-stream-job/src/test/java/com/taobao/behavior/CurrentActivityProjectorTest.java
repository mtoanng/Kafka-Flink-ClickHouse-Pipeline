package com.taobao.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taobao.behavior.avro.BehaviorType;
import com.taobao.behavior.model.UserCurrentActivity;
import com.taobao.behavior.processing.CurrentActivityProjector;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CurrentActivityProjectorTest {
    @Test
    void projectsOnlyNewerEventsForOneUser() {
        Optional<UserCurrentActivity> first =
                CurrentActivityProjector.project(
                        null,
                        EventTestSupport.event(100L, 500L, 50L, BehaviorType.cart, 10_000L, 1L),
                        20_000L);
        Optional<UserCurrentActivity> second =
                CurrentActivityProjector.project(
                        first.orElseThrow(),
                        EventTestSupport.event(100L, 501L, 51L, BehaviorType.buy, 20_000L, 2L),
                        30_000L);
        Optional<UserCurrentActivity> older =
                CurrentActivityProjector.project(
                        second.orElseThrow(),
                        EventTestSupport.event(100L, 499L, 49L, BehaviorType.pv, 15_000L, 3L),
                        40_000L);

        assertTrue(first.isPresent());
        assertTrue(second.isPresent());
        assertFalse(older.isPresent());
        UserCurrentActivity activity = second.orElseThrow();
        assertEquals(100L, activity.getUserId());
        assertEquals(20_000L, activity.getLastEventTimeMs());
        assertEquals("buy", activity.getLastBehaviorType());
        assertEquals(501L, activity.getLastItemId());
        assertEquals(51L, activity.getLastCategoryId());
        assertEquals(2L, activity.getSessionEventCount());
        assertEquals(30_000L, activity.getUpdatedAtMs());
    }

    @Test
    void sourceSequenceBreaksTiesAtTheSameEventTime() {
        UserCurrentActivity current =
                CurrentActivityProjector.project(
                                null,
                                EventTestSupport.event(100L, 500L, 50L, BehaviorType.cart, 10_000L, 9L),
                                20_000L)
                        .orElseThrow();

        assertFalse(
                CurrentActivityProjector.project(
                                current,
                                EventTestSupport.event(
                                        100L, 501L, 51L, BehaviorType.buy, 10_000L, 8L),
                                30_000L)
                        .isPresent());
        assertTrue(
                CurrentActivityProjector.project(
                                current,
                                EventTestSupport.event(
                                        100L, 501L, 51L, BehaviorType.buy, 10_000L, 10L),
                                30_000L)
                        .isPresent());
    }
}
