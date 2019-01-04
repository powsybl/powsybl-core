/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ColumnTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testPositiveColspan() {
        Column column = new Column("column");
        assertEquals(1, column.getColspan());

        column.setColspan(2);
        assertEquals(2, column.getColspan());
    }

    @Test
    public void testNegativeColspan() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("colspan must be greater than 0");

        new Column("column").setColspan(-5);
    }

}
