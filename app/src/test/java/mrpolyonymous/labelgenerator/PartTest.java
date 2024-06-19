package mrpolyonymous.labelgenerator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PartTest {

    @Test void testIdIgnoringPrint() {
        assertEquals("0", Part.idIgnoringPrint("0"));
        assertEquals("1", Part.idIgnoringPrint("1"));
        assertEquals("01", Part.idIgnoringPrint("01"));
        assertEquals("2454a", Part.idIgnoringPrint("2454a"));
        assertEquals("3622", Part.idIgnoringPrint("3622pr0004"));
        assertEquals("973c11h01", Part.idIgnoringPrint("973c11h01pr0001"));
        assertEquals("4865b", Part.idIgnoringPrint("4865bpr0007"));
        
        assertEquals("18394", Part.idIgnoringPrint("18394pat0002"));
        assertEquals("16709", Part.idIgnoringPrint("16709pats01"));
        assertEquals("16709", Part.idIgnoringPrint("16709pats01pr0001"));
    }

    
}
