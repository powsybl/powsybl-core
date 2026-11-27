/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class EqualsTester {

    private final List<List<Object>> groups = new ArrayList<>();

    public EqualsTester addEqualityGroup(Object... group) {
        assertTrue(group.length > 0, "An equality group must not be empty");
        groups.add(List.of(group));
        return this;
    }

    public void testEquals() {
        for (int i = 0; i < groups.size(); i++) {
            List<Object> group = groups.get(i);
            for (Object a : group) {
                // basic contract
                assertBasicContract(a);

                // same group => equal, symmetric, same hashCode
                assertEqualsInSameGroup(a, group);

                // different groups => not equal
                assertNotEqualsInOtherGroups(a, groups, i);
            }
        }
    }

    private static void assertBasicContract(Object a) {
        assertNotEquals(null, a, a + " must not be equal to null");
        assertNotEquals(new Object(), a, a + " must not be equal to an unrelated object");
        assertEquals(a, a, a + " must be equal to itself");
        assertEquals(a.hashCode(), a.hashCode(), "hashCode must be consistent");
    }

    private static void assertEqualsInSameGroup(Object a, List<Object> group) {
        for (Object b : group) {
            assertEquals(a, b, a + " should be equal to " + b);
            assertEquals(b, a, b + " should be equal to " + a);
            assertEquals(a.hashCode(), b.hashCode(), "hashCode differs for " + a + " and " + b);
        }
    }

    private static void assertNotEqualsInOtherGroups(Object a, List<List<Object>> groups, int groupIndex) {
        for (int j = 0; j < groups.size(); j++) {
            if (groupIndex == j) {
                continue;
            }
            for (Object other : groups.get(j)) {
                assertNotEquals(a, other, a + " should not be equal to " + other);
                assertNotEquals(other, a, other + " should not be equal to " + a);
            }
        }
    }
}
