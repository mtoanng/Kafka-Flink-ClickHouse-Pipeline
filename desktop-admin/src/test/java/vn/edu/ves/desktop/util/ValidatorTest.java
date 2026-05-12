package vn.edu.ves.desktop.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests cho Validator Strategy + Composite.
 */
public class ValidatorTest {

    @Test
    public void notBlankValidator_rejectsNullOrBlank() {
        Validator<String> v = new Validator.NotBlankValidator("Code");
        assertFalse(v.validate(null).isEmpty());
        assertFalse(v.validate("").isEmpty());
        assertFalse(v.validate("   ").isEmpty());
        assertTrue(v.validate("VN_NORTH").isEmpty());
    }

    @Test
    public void lengthRangeValidator_enforcesBounds() {
        Validator<String> v = new Validator.LengthRangeValidator("Name", 2, 10);
        assertTrue(v.validate(null).isEmpty()); // null skip
        assertFalse(v.validate("A").isEmpty());
        assertFalse(v.validate("A_VERY_LONG_NAME").isEmpty());
        assertTrue(v.validate("AB").isEmpty());
        assertTrue(v.validate("HelloWorld").isEmpty());
    }

    @Test
    public void patternValidator_matchesRegex() {
        Validator<String> v = new Validator.PatternValidator(
                "Region code", "^[A-Z0-9_]+$", "chỉ HOA + số + _");
        assertTrue(v.validate(null).isEmpty()); // null skip
        assertTrue(v.validate("VN_NORTH").isEmpty());
        assertTrue(v.validate("INTL_NA").isEmpty());
        assertFalse(v.validate("vn-north").isEmpty()); // lowercase + dash
        assertFalse(v.validate("Tên có dấu").isEmpty());
    }

    @Test
    public void inSetValidator_acceptsOnlyAllowedValues() {
        Validator<String> v = new Validator.InSetValidator("Zone", "BAC", "TRUNG", "NAM");
        assertTrue(v.validate("").isEmpty()); // blank skip
        assertTrue(v.validate(null).isEmpty());
        assertTrue(v.validate("BAC").isEmpty());
        assertFalse(v.validate("MIEN_BAC").isEmpty());
    }

    @Test
    public void compose_runsAllAndCollectsAllErrors() {
        Validator<String> chain = Validator.compose(
                new Validator.NotBlankValidator("Code"),
                new Validator.LengthRangeValidator("Code", 5, 20),
                new Validator.PatternValidator("Code", "^[A-Z]+$", "HOA only"));

        List<String> all = chain.validate("ab");
        assertEquals("NotBlank PASS (non-blank), Length FAIL, Pattern FAIL → 2 errors", 2, all.size());

        List<String> empty = chain.validate("VALID");
        assertEquals(0, empty.size());

        List<String> twoErrors = chain.validate("");
        assertEquals("NotBlank fail. Length & Pattern null-safe skip blank", 1, twoErrors.size());
    }
}
