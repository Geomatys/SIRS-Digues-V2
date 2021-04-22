/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author maximegavens
 */
public class AlphaNumComparatorTest {
    
    @Test
    public void testComparator() {
        AlphaNumComparator comparator = new AlphaNumComparator();
        
        // equal
        assertEquals(comparator.compare(null, null), 0);
        assertEquals(comparator.compare("3", "3"), 0);
        assertEquals(comparator.compare("0.2", "0.2"), 0);
        assertEquals(comparator.compare("test", "test"), 0);
        
        // Superior
        assertEquals(comparator.compare("0.1", null), 1);
        assertEquals(comparator.compare("test", null), 1);
        assertEquals(comparator.compare("2", "1"), 1);
        assertEquals(comparator.compare("0.1", "0.001"), 1);
        assertEquals(comparator.compare("12", "test"), 1);
        assertEquals(comparator.compare("34.43", "test"), 1);
        assertTrue(comparator.compare("b", "az") >= 1);
        
        // Inferior
        assertEquals(comparator.compare(null, "0.1"), -1);
        assertEquals(comparator.compare(null, "test"), -1);
        assertEquals(comparator.compare("1", "2"), -1);
        assertEquals(comparator.compare("0.3", "12.34567"), -1);
        assertEquals(comparator.compare("test", "12"), -1);
        assertEquals(comparator.compare("test", "12.34567"), -1);
        assertTrue(comparator.compare("a", "helloworldhelloworldhelloworldhelloworldhelloworld") <= -1);
    }
}
