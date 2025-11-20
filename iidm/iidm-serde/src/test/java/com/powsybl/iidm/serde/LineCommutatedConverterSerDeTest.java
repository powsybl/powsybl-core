/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class LineCommutatedConverterSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testNetworkLineCommutatedConverter() throws IOException {
        Network network = createBaseNetwork();

        // Test for the current version
        allFormatsRoundTripTest(network, "/lineCommutatedConverterRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // backward compatibility - checks from version 1.15
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("/lineCommutatedConverterRoundTripRef.xml", IidmVersion.V_1_15);
    }

    @Test
    void testNotSupported() throws IOException {
        Network network = createBaseNetwork();

        // Note: we do not test here failing for all versions < 1.15: LineCommutatedConverter cannot exist without DcNode,
        // hence the DcNode SerDe test is sufficient.

        // check it doesn't fail for version 1.14 if IidmVersionIncompatibilityBehavior is to log error
        var options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        testWriteVersionedXml(network, options, "lineCommutatedConverterNotSupported.xml", IidmVersion.V_1_14);
    }

    private static Network createBaseNetwork() {
        Network network = Network.create("lineCommutatedConverterTest", "code");
        network.setCaseDate(ZonedDateTime.parse("2025-01-02T03:04:05.000+01:00"));
        DcNode dcNode1 = network.newDcNode()
                .setId("dcNode1")
                .setNominalV(500.)
                .add();
        DcNode dcNode2 = network.newDcNode()
                .setId("dcNode2")
                .setNominalV(500.)
                .add();
        Substation s = network.newSubstation().setId("S").add();
        VoltageLevel vlBb400 = s.newVoltageLevel()
                .setId("vlBB400")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(400.)
                .add();
        Bus bus1 = vlBb400.getBusBreakerView()
                .newBus()
                .setId("bus1")
                .add();
        Bus bus2 = vlBb400.getBusBreakerView()
                .newBus()
                .setId("bus2")
                .add();
        VoltageLevel vlNb400 = s.newVoltageLevel()
                .setId("vlNB400")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400.)
                .add();
        vlNb400.getNodeBreakerView().newBusbarSection()
                .setId("bbs400-1")
                .setNode(0)
                .add();
        vlNb400.getNodeBreakerView().newBusbarSection()
                .setId("bbs400-2")
                .setNode(1)
                .add();
        vlNb400.getNodeBreakerView().newInternalConnection()
                .setNode1(0)
                .setNode2(2)
                .add();
        vlNb400.getNodeBreakerView().newInternalConnection()
                .setNode1(2)
                .setNode2(4)
                .add();
        vlNb400.getNodeBreakerView().newInternalConnection()
                .setNode1(1)
                .setNode2(3)
                .add();
        vlNb400.getNodeBreakerView().newInternalConnection()
                .setNode1(3)
                .setNode2(5)
                .add();
        Line lineBb = network.newLine()
                .setId("lineBb")
                .setR(0.).setX(3.).setB1(0.).setG1(0.).setB2(0.).setG2(0.)
                .setBus1(bus1.getId())
                .setBus2(bus2.getId())
                .add();
        Line lineNb = network.newLine()
                .setId("lineNb")
                .setR(0.).setX(3.).setB1(0.).setG1(0.).setB2(0.).setG2(0.)
                .setVoltageLevel1(vlNb400.getId())
                .setNode1(2)
                .setVoltageLevel2(vlNb400.getId())
                .setNode2(3)
                .add();
        LineCommutatedConverter lcc1 = vlBb400.newLineCommutatedConverter()
                .setId("lccBb1ACWithoutSolvedV")
                .setName("LCC in Bus/Breaker with one AC Terminal without solved values")
                .setDcNode1(dcNode1.getId())
                .setDcConnected1(false)
                .setDcNode2(dcNode2.getId())
                .setDcConnected2(false)
                .setConnectableBus1(bus1.getId())
                .setReactiveModel(LineCommutatedConverter.ReactiveModel.FIXED_POWER_FACTOR)
                .setPowerFactor(0.92)
                .setControlMode(AcDcConverter.ControlMode.V_DC)
                .setTargetVdc(502.)
                .add();
        lcc1.setProperty("prop name", "prop value");
        lcc1.addAlias("someAlias");

        LineCommutatedConverter lcc2 = vlBb400.newLineCommutatedConverter()
                .setId("lccBb2ACWithSolvedV")
                .setName("LCC in Bus/Breaker with two AC Terminals with solved values and droop curve")
                .setDcNode1(dcNode1.getId())
                .setDcConnected1(true)
                .setDcNode2(dcNode2.getId())
                .setDcConnected2(true)
                .setBus1(bus1.getId())
                .setBus2(bus2.getId())
                .setReactiveModel(LineCommutatedConverter.ReactiveModel.CALCULATED_POWER_FACTOR)
                .setPowerFactor(0.92)
                .setControlMode(AcDcConverter.ControlMode.P_PCC_DROOP)
                .setTargetVdc(502.)
                .setTargetP(301.)
                .setPccTerminal(lineBb.getTerminal1())
                .setIdleLoss(2.0)
                .setSwitchingLoss(0.2)
                .setResistiveLoss(2e-6)
                .add();
        lcc2.getDcTerminal1().setP(-100.).setI(-200.);
        lcc2.getDcTerminal2().setP(0.4).setI(200.);
        lcc2.getTerminal1().setP(-100.).setQ(-200.);
        lcc2.getTerminal2().orElseThrow().setP(-100.1).setQ(-200.2);

        lcc2.newDroopCurve()
                .beginSegment().setK(-10.).setMinV(-500.).setMaxV(-100.).endSegment()
                .beginSegment().setK(-5.).setMinV(-100.).setMaxV(100.).endSegment()
                .beginSegment().setK(-1.).setMinV(100.).setMaxV(500.).endSegment()
                .add();

        LineCommutatedConverter lcc3 = vlNb400.newLineCommutatedConverter()
                .setId("lccNb2ACWithPartiallySolvedV")
                .setName("LCC in Node/Breaker with two AC Terminals with some solved values missing")
                .setDcNode1(dcNode1.getId())
                .setDcConnected1(true)
                .setDcNode2(dcNode2.getId())
                .setDcConnected2(true)
                .setNode1(4)
                .setNode2(5)
                .setReactiveModel(LineCommutatedConverter.ReactiveModel.FIXED_POWER_FACTOR)
                .setPowerFactor(0.94)
                .setControlMode(AcDcConverter.ControlMode.P_PCC)
                .setTargetVdc(503.)
                .setTargetP(302.)
                .setPccTerminal(lineNb.getTerminal2())
                .setIdleLoss(3.0)
                .setSwitchingLoss(0.3)
                .setResistiveLoss(3e-6)
                .add();

        lcc3.getTerminal1().getBusView().getBus().setV(402.).setAngle(12.3);
        lcc3.getDcTerminal1().setP(-102.); // no I
        lcc3.getDcTerminal2().setI(202.); // no P
        lcc3.getTerminal1().setP(-105.); // no Q
        lcc3.getTerminal2().orElseThrow().setQ(-200.8); // no P

        return network;
    }

}
