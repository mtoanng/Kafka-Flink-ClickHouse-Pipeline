package vn.edu.ves.desktop.widget;

import javafx.scene.paint.Color;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/**
 * Pure-logic tests for the score → colour / status mapping used by VietnamMap.
 *
 * <p>No JavaFX runtime needed since {@link VietnamMap#colorForScore(BigDecimal)}
 * and {@link VietnamMap#statusForScore(BigDecimal)} are static and Color is value-typed.</p>
 */
public class VietnamMapTest {

    @Test
    public void colorForScore_securePalette() {
        assertEquals(Color.web("#2E7D32"), VietnamMap.colorForScore(new BigDecimal("95.0")));
        assertEquals(Color.web("#2E7D32"), VietnamMap.colorForScore(new BigDecimal("80.0")));
    }

    @Test
    public void colorForScore_elevatedPalette() {
        assertEquals(Color.web("#F9A825"), VietnamMap.colorForScore(new BigDecimal("75.0")));
        assertEquals(Color.web("#F9A825"), VietnamMap.colorForScore(new BigDecimal("60.0")));
    }

    @Test
    public void colorForScore_stressedPalette() {
        assertEquals(Color.web("#EF6C00"), VietnamMap.colorForScore(new BigDecimal("55.0")));
        assertEquals(Color.web("#EF6C00"), VietnamMap.colorForScore(new BigDecimal("40.0")));
    }

    @Test
    public void colorForScore_criticalPalette() {
        assertEquals(Color.web("#C62828"), VietnamMap.colorForScore(new BigDecimal("39.9")));
        assertEquals(Color.web("#C62828"), VietnamMap.colorForScore(new BigDecimal("0.0")));
    }

    @Test
    public void colorForScore_nullReturnsNeutralGrey() {
        assertEquals(Color.web("#B0BEC5"), VietnamMap.colorForScore(null));
    }

    @Test
    public void statusForScore_thresholdLadder() {
        assertEquals("SECURE",   VietnamMap.statusForScore(new BigDecimal("90")));
        assertEquals("ELEVATED", VietnamMap.statusForScore(new BigDecimal("65")));
        assertEquals("STRESSED", VietnamMap.statusForScore(new BigDecimal("45")));
        assertEquals("CRITICAL", VietnamMap.statusForScore(new BigDecimal("10")));
        assertEquals("NO_DATA",  VietnamMap.statusForScore(null));
    }
}
