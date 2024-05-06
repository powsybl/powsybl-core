/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.ExportOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ConnectablePositionXmlTest extends AbstractIidmSerDeTest {

    private static Network createTestNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
        vl.newGenerator()
                .setId("G")
                .setNode(1)
                .setEnergySource(EnergySource.NUCLEAR)
                .setMinP(10)
                .setMaxP(20)
                .setTargetP(20)
                .setTargetV(400)
                .setVoltageRegulatorOn(true)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("SW")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        network.newLine()
                .setId("L")
                .setVoltageLevel1("VL")
                .setNode1(2)
                .setVoltageLevel2("VL2")
                .setNode2(0)
                .setR(1)
                .setX(1)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        return network;
    }

    @Test
    void test() throws IOException {
        Network network = createTestNetwork();

        // extend generator
        Generator generator = network.getGenerator("G");
        assertNotNull(generator);
        generator.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withOrder(10)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .add();

        // extend line
        Line line = network.getLine("L");
        assertNotNull(line);
        line.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("L")
                .withOrder(10)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add()
                .newFeeder2()
                .withName("L")
                .withOrder(20)
                .withDirection(ConnectablePosition.Direction.BOTTOM)
                .add()
                .add();

        Network network2 = allFormatsRoundTripTest(network, "/connectablePositionRef_V1_1.xml");

        Generator generator2 = network2.getGenerator("G");
        assertNotNull(generator);
        ConnectablePosition<Generator> generatorPosition2 = generator2.getExtension(ConnectablePosition.class);
        assertNotNull(generatorPosition2);
        assertNotNull(generatorPosition2.getFeeder());
        assertNull(generatorPosition2.getFeeder1());
        assertNull(generatorPosition2.getFeeder2());
        assertNull(generatorPosition2.getFeeder3());
        assertEquals(10, (int) generatorPosition2.getFeeder().getOrder().orElse(-1));
        assertEquals(ConnectablePosition.Direction.TOP, generatorPosition2.getFeeder().getDirection());

        Line line2 = network2.getLine("L");
        assertNotNull(line2);
        ConnectablePosition<Line> linePosition2 = line2.getExtension(ConnectablePosition.class);
        assertNotNull(linePosition2);
        assertNull(linePosition2.getFeeder());
        assertNotNull(linePosition2.getFeeder1());
        assertNotNull(linePosition2.getFeeder2());
        assertNull(linePosition2.getFeeder3());
        assertEquals(10, (int) linePosition2.getFeeder1().getOrder().orElse(-1));
        assertEquals(ConnectablePosition.Direction.TOP, linePosition2.getFeeder1().getDirection());
        assertEquals(20, (int) linePosition2.getFeeder2().getOrder().orElse(-1));
        assertEquals(ConnectablePosition.Direction.BOTTOM, linePosition2.getFeeder2().getDirection());

        linePosition2.getFeeder1().setDirection(ConnectablePosition.Direction.BOTTOM);
        linePosition2.getFeeder1().setOrder(20);
        assertEquals(linePosition2.getFeeder1().getDirection(), linePosition2.getFeeder2().getDirection());
        assertEquals(linePosition2.getFeeder1().getOrder(), linePosition2.getFeeder2().getOrder());

        // test v 1.0
        allFormatsRoundTripTest(network, "/connectablePositionRef_V1_0.xml",
                new ExportOptions().addExtensionVersion(ConnectablePosition.NAME, "1.0"));
    }
}
