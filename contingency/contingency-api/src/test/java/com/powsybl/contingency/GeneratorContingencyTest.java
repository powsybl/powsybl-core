/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.tasks.GeneratorTripping;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class GeneratorContingencyTest {

    @Test
    public void test() {
        GeneratorContingency contingency = new GeneratorContingency("id");
        assertEquals("id", contingency.getId());
        assertEquals(ContingencyElementType.GENERATOR, contingency.getType());

        assertNotNull(contingency.toTask());
        assertTrue(contingency.toTask() instanceof GeneratorTripping);

        new EqualsTester()
                .addEqualityGroup(new GeneratorContingency("g1"), new GeneratorContingency("g1"))
                .addEqualityGroup(new GeneratorContingency("g2"), new GeneratorContingency("g2"))
                .testEquals();
    }
}
