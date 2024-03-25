/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util.trove;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TBooleanArrayListTest {

    @Test
    void test() {
        TBooleanArrayList list = new TBooleanArrayList(1);
        assertEquals(0, list.size());
        list.add(false);
        assertEquals(1, list.size());
        assertFalse(list.get(0));
        list.ensureCapacity(10);
        list.removeAt(0);
        assertEquals(0, list.size());
        list.fill(0, 3, true);
        assertEquals(3, list.size());
        list.remove(1, 1);
        assertEquals(2, list.size());
        list.set(0, false);
        assertFalse(list.get(0));
    }
}
