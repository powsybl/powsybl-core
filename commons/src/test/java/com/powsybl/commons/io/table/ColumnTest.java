/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io.table;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ColumnTest {

    @Test
    void testPositiveColspan() {
        Column column = new Column("column");
        assertEquals(1, column.getColspan());

        column.setColspan(2);
        assertEquals(2, column.getColspan());
    }

    @Test
    void testNegativeColspan() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new Column("column").setColspan(-5));
        assertEquals("colspan must be greater than 0", e.getMessage());
    }

}
