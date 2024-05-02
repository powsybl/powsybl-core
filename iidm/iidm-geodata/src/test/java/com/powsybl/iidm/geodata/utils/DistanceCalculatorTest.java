
package com.powsybl.iidm.geodata.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Hugo Kulesza <hugo.kulesza at rte-france.comp>
 */
public class DistanceCalculatorTest {

    @Test
    public void test() {
        double zeroDistance = DistanceCalculator.distance(1, 0, 1, 0);
        assertEquals(0, zeroDistance);

        double nonZeroDistance = DistanceCalculator.distance(10, 10, 20, 20);
        assertEquals(1546488.0483491954, nonZeroDistance);
    }
}
