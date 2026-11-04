/**
 * Copyright (c) 2026, Kick Stage d.o.o.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedance;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedanceAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Pascal Ehlert {@literal <pascal.ehlert at kickstage.com>}
 */
public abstract class AbstractLineNonReciprocalImpedanceTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        LineNonReciprocalImpedance extension = line.newExtension(LineNonReciprocalImpedanceAdder.class)
                .withR21(-1.2)
                .withX21(35.1)
                .add();
        assertEquals("lineNonReciprocalImpedance", extension.getName());
        assertEquals(-1.2, extension.getR21(), 0);
        assertEquals(35.1, extension.getX21(), 0);

        extension.setR21(2.9);
        extension.setX21(31.0);
        assertEquals(2.9, extension.getR21(), 0);
        assertEquals(31.0, extension.getX21(), 0);
    }

    @Test
    public void testIncomplete() {
        Network network = EurostagTutorialExample1Factory.create();
        LineNonReciprocalImpedanceAdder adder = network.getLine("NHV1_NHV2_1").newExtension(LineNonReciprocalImpedanceAdder.class)
                .withR21(-1.2);
        PowsyblException e = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Line 'NHV1_NHV2_1': r21 and x21 are required", e.getMessage());
    }
}
