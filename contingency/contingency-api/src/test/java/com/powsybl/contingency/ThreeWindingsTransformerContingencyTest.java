/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.iidm.modification.tripping.ThreeWindingsTransformerTripping;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
class ThreeWindingsTransformerContingencyTest {

    @Test
    void test() {
        var twt3Contingency = new ThreeWindingsTransformerContingency("twt3");
        assertEquals("twt3", twt3Contingency.getId());
        assertEquals(ContingencyElementType.THREE_WINDINGS_TRANSFORMER, twt3Contingency.getType());

        assertNotNull(twt3Contingency.toModification());
        assertTrue(twt3Contingency.toModification() instanceof ThreeWindingsTransformerTripping);

        new EqualsTester()
                .addEqualityGroup(new ThreeWindingsTransformerContingency("foo"), new ThreeWindingsTransformerContingency("foo"))
                .addEqualityGroup(new ThreeWindingsTransformerContingency("bar"), new ThreeWindingsTransformerContingency("bar"))
                .testEquals();
    }
}
