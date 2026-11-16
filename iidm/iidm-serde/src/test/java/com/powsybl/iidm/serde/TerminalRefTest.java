/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

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
        PowsyblException e = assertThrows(PowsyblException.class, () -> TerminalRefSerDe.resolve("S1VL1_LD1_BREAKER", ThreeSides.ONE, null, network));
        assertEquals("Unexpected terminal reference identifiable instance: class com.powsybl.iidm.network.impl.SwitchImpl", e.getMessage());
    }

    @Test
    void badBranchSideResolveTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        PowsyblException e = assertThrows(PowsyblException.class, () -> TerminalRefSerDe.resolve("LINE_S2S3", ThreeSides.THREE, null, network));
        assertEquals("Cannot convert ThreeSides value THREE as a TwoSides (ONE, TWO)", e.getMessage());
    }

    @Test
    void badIdentifiableSideResolveTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        PowsyblException e = assertThrows(PowsyblException.class, () -> TerminalRefSerDe.resolve("LIN_S2S3", ThreeSides.ONE, null, network));
        assertEquals("Terminal reference identifiable not found: 'LIN_S2S3'", e.getMessage());
    }

    @Test
    void badTerminalBothSideAndNumberResolveTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        PowsyblException e = assertThrows(PowsyblException.class, () -> TerminalRefSerDe.resolve("LINE_S2S3", ThreeSides.ONE, TerminalNumber.ONE, network));
        assertEquals("Terminal reference specifies both terminal side and terminal number: 'LINE_S2S3'", e.getMessage());
    }

    @Test
    void acDcConverterResolveTest() {
        Network network = DcDetailedNetworkFactory.createLccMonopoleGroundReturn();
        String lccFr = "LccFr";
        LineCommutatedConverter converter = network.getLineCommutatedConverter(lccFr);
        Terminal t1 = TerminalRefSerDe.resolve(lccFr, null, TerminalNumber.ONE, network);
        Terminal t2 = TerminalRefSerDe.resolve(lccFr, null, TerminalNumber.TWO, network);
        assertSame(converter.getTerminal1(), t1);
        assertSame(converter.getTerminal2().orElseThrow(), t2);
    }

    @Test
    void badAcDcConverterTerminalNumberResolveTest() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        String vscFr = "VscFr";
        VoltageSourceConverter converter = network.getVoltageSourceConverter(vscFr);
        Terminal t1 = TerminalRefSerDe.resolve(converter.getId(), null, TerminalNumber.ONE, network);
        assertSame(converter.getTerminal1(), t1);
        PowsyblException e = assertThrows(PowsyblException.class, () -> TerminalRefSerDe.resolve(vscFr, null, TerminalNumber.TWO, network));
        assertEquals("This AC/DC converter does not have a second AC Terminal: VscFr", e.getMessage());
    }
}
