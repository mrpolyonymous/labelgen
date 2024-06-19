package mrpolyonymous.labelgenerator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class UtilsTest {
    @Test void testTrimZero() {
        String id = "0";
        String trimmed = Utils.trimLeadingZeros(id);
        assertEquals("0", trimmed);
    }

    @Test void testTrimMultipleZeros() {
        String id = "0000";
        String trimmed = Utils.trimLeadingZeros(id);
        assertEquals("0", trimmed);
    }

    @Test void testNoLeadingZero() {
        String id = "1234";
        String trimmed = Utils.trimLeadingZeros(id);
        assertEquals(id, trimmed);
    }

    @Test void testLeadingZero() {
        String id = "001234";
        String trimmed = Utils.trimLeadingZeros(id);
        assertEquals("1234", trimmed);
    }

    @Test void testEmpty() {
        String id = "";
        String trimmed = Utils.trimLeadingZeros(id);
        assertEquals(id, trimmed);
    }

}
