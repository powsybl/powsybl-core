/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

class RatioTapChangerSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        allFormatsRoundTripTest(createTestNetwork(), "ratioTapChangerReactivePowerControlRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void readFaultyVersionRatioTapChangerFile() {
        testForAllPreviousVersions(IidmVersion.V_1_12, version -> {
            InputStream is = getVersionedNetworkAsStream("ratioTapChangerReactivePowerControlRef.xml", version);
            assertThrows(NullPointerException.class, () -> NetworkSerDe.read(is));
        });
    }

    @Test
    void roundTripWithSolvedTapPosition() throws IOException {
        allFormatsRoundTripTest(createTestNetwork(0), "ratioTapChangerReactivePowerControlRefWithSolvedTapPosition.xml", CURRENT_IIDM_VERSION);
    }

    Network createTestNetwork() {
        return createTestNetwork(null);
    }

    Network createTestNetwork(Integer solvedTapPosition) {
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2024-01-08T09:17:53.764Z"));

        Substation substation = network.newSubstation()
                .setId("SUBSTATION")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = substation.newVoltageLevel()
                .setId("VL_1")
                .setNominalV(132.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("BUS_1")
                .add();
        VoltageLevel vl2 = substation.newVoltageLevel()
                .setId("VL_2")
                .setNominalV(33.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("BUS_2")
                .add();

        TwoWindingsTransformer t2wt = network.getSubstation("SUBSTATION").newTwoWindingsTransformer()
                .setId("T2wT")
                .setRatedU1(132.0)
                .setRatedU2(33.0)
                .setR(17.0)
                .setX(10.0)
                .setG(0.00573921028466483)
                .setB(0.000573921028466483)
                .setBus1("BUS_1")
                .setBus2("BUS_2")
                .add();

        t2wt.newRatioTapChanger()
                .beginStep()
                .setRho(0.9)
                .setR(0.1089)
                .setX(0.01089)
                .setG(0.8264462809917356)
                .setB(0.08264462809917356)
                .endStep()
                .setTapPosition(0)
                .setSolvedTapPosition(solvedTapPosition)
                .setLoadTapChangingCapabilities(true)
                .setRegulating(true)
                .setRegulationMode(RatioTapChanger.RegulationMode.REACTIVE_POWER)
                .setRegulationValue(-10)
                .setRegulationTerminal(t2wt.getTerminal2())
                .setTargetDeadband(0)
                .add();

        return network;
    }

}
