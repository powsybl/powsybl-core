/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class TerminalRefTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTerminalRefTest() throws IOException {
        allFormatsRoundTripAllVersionedXmlTest("terminalRef.xiidm");
    }

    @Test
    void roundTripRegulatingTerminalTest() throws IOException {
        allFormatsRoundTripAllVersionedXmlTest("regulatingTerminal.xml");
    }

    @Test
    void badSwitchTerminalResolveTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        PowsyblException e = assertThrows(PowsyblException.class, () -> TerminalRefSerDe.resolve("S1VL1_LD1_BREAKER", ThreeSides.ONE, network));
        assertEquals("Unexpected terminal reference identifiable instance: class com.powsybl.iidm.network.impl.SwitchImpl", e.getMessage());
    }

    @Test
    void badBranchSideResolveTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        PowsyblException e = assertThrows(PowsyblException.class, () -> TerminalRefSerDe.resolve("LINE_S2S3", ThreeSides.THREE, network));
        assertEquals("Cannot convert ThreeSides value THREE as a TwoSides (ONE, TWO)", e.getMessage());
    }

    @Test
    void badIdentifiableSideResolveTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        PowsyblException e = assertThrows(PowsyblException.class, () -> TerminalRefSerDe.resolve("LIN_S2S3", ThreeSides.ONE, network));
        assertEquals("Terminal reference identifiable not found: 'LIN_S2S3'", e.getMessage());
    }
}
