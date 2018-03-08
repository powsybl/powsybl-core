package com.powsybl.action.simulator.parallel;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class FiltrationTest {
    @Test
    public void test() {
        Filtration f1of1 = new Filtration("1/1");
        assertEquals(0, f1of1.from(1));
        assertEquals(1, f1of1.to(1));
        assertEquals(0, f1of1.from(2));
        assertEquals(2, f1of1.to(2));

        Filtration f1of2 = new Filtration("1/2");
        Filtration f2of2 = new Filtration("2/2");
        // total:2
        // [0,1) [1,2)
        assertEquals(0, f1of2.from(2));
        assertEquals(1, f1of2.to(2));
        assertEquals(1, f2of2.from(2));
        assertEquals(2, f2of2.to(2));

        Filtration f1of3 = new Filtration("1/3");
        Filtration f2of3 = new Filtration("2/3");
        Filtration f3of3 = new Filtration("3/3");
        // total:5
        // [0,1) [1,3) [3,5)
        assertEquals(0, f1of3.from(5));
        assertEquals(1, f1of3.to(5)); // 1 * 5 / 3
        assertEquals(1, f2of3.from(5)); // (2-1) * 5 / 3
        assertEquals(3, f2of3.to(5)); // 2 * 5 / 3
        assertEquals(3, f3of3.from(5)); // (3-1) * 5 / 3
        assertEquals(5, f3of3.to(5)); // 3 * 5 / 3
    }
}
