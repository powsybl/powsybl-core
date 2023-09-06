/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TerminalRef;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class TerminalRefTest extends AbstractXmlConverterTest {

    @Test
    void roundTripTest() throws IOException {
        roundTripAllVersionedXmlTest("terminalRef.xiidm");
        roundTripAllVersionedXmlTest("regulatingTerminal.xml");
    }

    @Test
    void badSwitchTerminalResolveTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        PowsyblException e = assertThrows(PowsyblException.class, () -> TerminalRef.resolve("S1VL1_LD1_BREAKER", TerminalRef.Side.ONE, network));
        assertEquals("Unexpected terminal reference identifiable instance: class com.powsybl.iidm.network.impl.SwitchImpl", e.getMessage());
    }

    @Test
    void badBranchSideResolveTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> TerminalRef.resolve("LINE_S2S3", TerminalRef.Side.THREE, network));
        assertEquals("Unexpected Branch side: THREE", e.getMessage());
    }

    @Test
    void badIdentifiableSideResolveTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        PowsyblException e = assertThrows(PowsyblException.class, () -> TerminalRef.resolve("LIN_S2S3", TerminalRef.Side.ONE, network));
        assertEquals("Terminal reference identifiable not found: 'LIN_S2S3'", e.getMessage());
    }
}
