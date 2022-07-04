/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.impl.extensions.ConnectablePositionImpl;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ConnectablePositionXmlTest extends AbstractConverterTest {

    private static Network createTestNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));
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
    public void test() throws IOException {
        Network network = createTestNetwork();

        // extend generator
        Generator generator = network.getGenerator("G");
        assertNotNull(generator);
        ConnectablePosition<Generator> generationPosition = new ConnectablePositionImpl<>(generator,
                new ConnectablePositionImpl.FeederImpl("G", 10, ConnectablePosition.Direction.TOP),
                null,
                null,
                null);
        generator.addExtension(ConnectablePosition.class, generationPosition);

        // extend line
        Line line = network.getLine("L");
        assertNotNull(line);
        ConnectablePosition<Line> linePosition = new ConnectablePositionImpl<>(line,
                null,
                new ConnectablePositionImpl.FeederImpl("L", 10, ConnectablePosition.Direction.TOP),
                new ConnectablePositionImpl.FeederImpl("L", 20, ConnectablePosition.Direction.BOTTOM),
                null);
        line.addExtension(ConnectablePosition.class, linePosition);

        Network network2 = roundTripXmlTest(network,
                                            NetworkXml::writeAndValidate,
                                            NetworkXml::read,
                                            "/connectablePositionRef.xml");

        Generator generator2 = network2.getGenerator("G");
        assertNotNull(generator);
        ConnectablePosition generatorPosition2 = generator2.getExtension(ConnectablePosition.class);
        assertNotNull(generatorPosition2);
        assertNotNull(generatorPosition2.getFeeder());
        assertNull(generatorPosition2.getFeeder1());
        assertNull(generatorPosition2.getFeeder2());
        assertNull(generatorPosition2.getFeeder3());
        assertEquals(generatorPosition2.getFeeder().getOrder(), generationPosition.getFeeder().getOrder());
        assertEquals(generatorPosition2.getFeeder().getDirection(), generationPosition.getFeeder().getDirection());

        Line line2 = network2.getLine("L");
        assertNotNull(line2);
        ConnectablePosition linePosition2 = line2.getExtension(ConnectablePosition.class);
        assertNotNull(linePosition2);
        assertNull(linePosition2.getFeeder());
        assertNotNull(linePosition2.getFeeder1());
        assertNotNull(linePosition2.getFeeder2());
        assertNull(linePosition2.getFeeder3());
        assertEquals(linePosition2.getFeeder1().getOrder(), linePosition2.getFeeder1().getOrder());
        assertEquals(linePosition2.getFeeder1().getDirection(), linePosition2.getFeeder1().getDirection());
        assertEquals(linePosition2.getFeeder2().getOrder(), linePosition2.getFeeder2().getOrder());
        assertEquals(linePosition2.getFeeder2().getDirection(), linePosition2.getFeeder2().getDirection());
    }
}
