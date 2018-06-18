package com.powsybl.computation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PartitionTest {
    @Test
    public void test() {
        Partition p1of1 = new Partition("1/1");
        assertEquals(0, p1of1.startIndex(1));
        assertEquals(1, p1of1.endIndex(1));
        assertEquals(0, p1of1.startIndex(2));
        assertEquals(2, p1of1.endIndex(2));

        Partition p1of2 = new Partition("1/2");
        Partition p2of2 = new Partition("2/2");
        // total:2
        // [0,1) [1,2)
        assertEquals(0, p1of2.startIndex(2));
        assertEquals(1, p1of2.endIndex(2));
        assertEquals(1, p2of2.startIndex(2));
        assertEquals(2, p2of2.endIndex(2));

        Partition p1of3 = new Partition("1/3");
        Partition p2of3 = new Partition("2/3");
        Partition p3of3 = new Partition("3/3");
        // total:5
        // [0,1) [1,3) [3,5)
        assertEquals(0, p1of3.startIndex(5));
        assertEquals(1, p1of3.endIndex(5)); // 1 * 5 / 3
        assertEquals(1, p2of3.startIndex(5)); // (2-1) * 5 / 3
        assertEquals(3, p2of3.endIndex(5)); // 2 * 5 / 3
        assertEquals(3, p3of3.startIndex(5)); // (3-1) * 5 / 3
        assertEquals(5, p3of3.endIndex(5)); // 3 * 5 / 3
    }
}
