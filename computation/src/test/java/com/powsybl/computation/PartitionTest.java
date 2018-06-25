/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class PartitionTest {

    @Test
    public void test() {
        Partition p1of1 = Partition.parse("1/1");
        assertEquals(0, p1of1.startIndex(1));
        assertEquals(1, p1of1.endIndex(1));
        assertEquals(0, p1of1.startIndex(2));
        assertEquals(2, p1of1.endIndex(2));

        Partition p1of2 = Partition.parse("1/2");
        Partition p2of2 = Partition.parse("2/2");
        // total:2
        // [0,1) [1,2)
        assertEquals(0, p1of2.startIndex(2));
        assertEquals(1, p1of2.endIndex(2));
        assertEquals(1, p2of2.startIndex(2));
        assertEquals(2, p2of2.endIndex(2));

        Partition p1of3 = Partition.parse("1/3");
        Partition p2of3 = Partition.parse("2/3");
        Partition p3of3 = Partition.parse("3/3");
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
