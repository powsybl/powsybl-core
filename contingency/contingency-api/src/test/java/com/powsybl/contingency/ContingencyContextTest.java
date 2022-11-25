/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ContingencyContextTest {

    @Test
    public void test() {
        ContingencyContext context = new ContingencyContext("c1", ContingencyContextType.SPECIFIC);
        assertEquals("ContingencyContext(contingencyId='c1', contextType=SPECIFIC)", context.toString());
    }
}
