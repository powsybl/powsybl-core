/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.network.compare;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class IssuesTest {

    @Test
    void testFixIdentifiablesEquivalentIfBothNull() {
        Network n = createNetwork();
        Comparison c = new Comparison(n, n, new ComparisonConfig());

        // Network should compare with itself
        c.compare();

        // When the generator and line are disconnected
        // the bus breaker view from the regulating terminal of the generator
        // returns null
        // After the fix made in comparison network should compare with itself
        // Previously two identifiables equal to null made comparison fail
        n.getGenerator("G1").getTerminal().disconnect();
        n.getLine("Line12").getTerminal2().disconnect();
        // NOTE:
        // Disconnecting the terminal of the generator or the regulated terminal
        // do not deactivate voltage regulation
        assertTrue(n.getGenerator("G1").isVoltageRegulatorOn());
        c.compare();
    }

    private static Network createNetwork() {
        // For the buses to be valid they have to be connected to at least one branch
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.ES)
            .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
            .setId("VL1")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Substation s2 = network.newSubstation()
            .setId("S2")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
            .setId("VL2")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vl1.getBusBreakerView().newBus()
            .setId("B1")
            .add();
        Generator g1 = vl1.newGenerator()
            .setId("G1")
            .setBus("B1")
            .setMinP(0)
            .setMaxP(1)
            .setTargetP(1)
            .setTargetQ(0)
            .setVoltageRegulatorOn(true)
            .setTargetV(400)
            .add();
        assertEquals(g1.getRegulatingTerminal(), g1.getTerminal());
        vl2.getBusBreakerView().newBus()
            .setId("B2")
            .add();
        vl2.newLoad()
            .setId("L2")
            .setBus("B2")
            .setP0(1)
            .setQ0(0)
            .add();
        network.newLine()
            .setId("Line12")
            .setVoltageLevel1("VL1")
            .setVoltageLevel2("VL2")
            .setBus1("B1")
            .setBus2("B2")
            .setR(1)
            .setX(1)
            .setG1(0)
            .setB1(0)
            .setG2(0)
            .setB2(0)
            .add();
        return network;
    }
}
