/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.apache.commons.lang3.Range;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsAfter;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsBefore;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.TOP;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class CreateBranchFeederBaysTest extends AbstractConverterTest {

    private final Network bbNetwork = EurostagTutorialExample1Factory.create().setCaseDate(DateTime.parse("2013-01-15T18:45:00.000+01:00"));
    private final Network nbNetwork = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

    @Test
    void baseNodeBreakerLineTest() throws IOException {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs5")
                .withPositionOrder1(85)
                .withDirection1(BOTTOM)
                .withFeederName1("lineTestFeeder1")
                .withBusOrBusbarSectionId2("bbs1")
                .withPositionOrder2(75)
                .withFeederName2("lineTestFeeder2")
                .withDirection2(TOP)
                .build();
        modification.apply(nbNetwork);
        roundTripXmlTest(nbNetwork, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-line.xml");
    }

    @Test
    void baseBusBreakerLineTest() throws IOException {
        LineAdder lineAdder = bbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("NGEN")
                .withPositionOrder1(85)
                .withDirection1(BOTTOM)
                .withFeederName1("lineTestFeeder1")
                .withBusOrBusbarSectionId2("NHV1")
                .withFeederName2("lineTestFeeder2")
                .withDirection2(TOP)
                .build();
        modification.apply(bbNetwork);
        roundTripXmlTest(bbNetwork, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-create-line-feeder-bays.xml");
    }

    @Test
    void usedOrderLineTest() throws IOException {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs1")
                .withPositionOrder1(70)
                .withDirection1(TOP)
                .withBusOrBusbarSectionId2("bbs5")
                .withPositionOrder2(85)
                .withDirection2(BOTTOM)
                .build();
        modification.apply(nbNetwork);
        roundTripTest(nbNetwork, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-line-order-used.xml");
    }

    @Test
    void baseInternalLineTest() throws IOException {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs2")
                .withPositionOrder1(105)
                .withDirection1(TOP)
                .withBusOrBusbarSectionId2("bbs1")
                .withPositionOrder2(14)
                .withDirection2(BOTTOM)
                .build();
        modification.apply(nbNetwork);
        roundTripXmlTest(nbNetwork, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-internal-line.xml");
    }

    @Test
    void getUnusedOrderPositionAfter() {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        Optional<Range<Integer>> unusedOrderPositionsAfter = getUnusedOrderPositionsAfter(nbNetwork.getBusbarSection("bbs2"));
        assertTrue(unusedOrderPositionsAfter.isPresent());
        assertEquals(121, (int) unusedOrderPositionsAfter.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter.get().getMaximum());

        int positionOrder1 = unusedOrderPositionsAfter.get().getMinimum();
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs1")
                .withPositionOrder1(positionOrder1)
                .withDirection1(TOP)
                .withBusOrBusbarSectionId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        modification.apply(nbNetwork);

        ConnectablePosition<Line> newLine = nbNetwork.getLine("lineTest").getExtension(ConnectablePosition.class);
        assertEquals(TOP, newLine.getFeeder1().getDirection());
        assertEquals(Optional.of(121), newLine.getFeeder1().getOrder());
    }

    @Test
    void getUnusedOrderPositionBefore() {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        Optional<Range<Integer>> unusedOrderPositionsBefore = getUnusedOrderPositionsBefore(nbNetwork.getBusbarSection("bbs2"));
        assertTrue(unusedOrderPositionsBefore.isPresent());
        assertEquals(71, (int) unusedOrderPositionsBefore.get().getMinimum());
        assertEquals(79, (int) unusedOrderPositionsBefore.get().getMaximum());
        int positionOrder1 = unusedOrderPositionsBefore.get().getMaximum();

        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs2")
                .withPositionOrder2(positionOrder1)
                .withDirection2(BOTTOM)
                .build();
        modification.apply(nbNetwork);

        ConnectablePosition<Line> newLine = nbNetwork.getLine("lineTest").getExtension(ConnectablePosition.class);
        assertEquals(BOTTOM, newLine.getFeeder2().getDirection());
        assertEquals(Optional.of(79), newLine.getFeeder2().getOrder());
    }

    @Test
    void testException() {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);

        //wrong network
        Network network1 = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        CreateBranchFeederBays modification0 = new CreateBranchFeederBaysBuilder().
                withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs1")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> modification0.apply(network1, true, Reporter.NO_OP));
        assertEquals("Network given in parameters and in connectableAdder are different. Connectable was added then removed", e0.getMessage());

        // not found id
        CreateBranchFeederBays modification1 = new CreateBranchFeederBaysBuilder().
                withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> modification1.apply(nbNetwork, true, Reporter.NO_OP));
        assertEquals("Identifiable bbs not found.", e1.getMessage());

        // wrong identifiable type
        CreateBranchFeederBays modification2 = new CreateBranchFeederBaysBuilder().
                withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("gen1")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification2.apply(nbNetwork, true, Reporter.NO_OP));
        assertEquals("Unsupported type GENERATOR for identifiable gen1", e2.getMessage());
    }

    @Test
    void baseNodeBreakerTwoWindingsTransformerTest() throws IOException {
        TwoWindingsTransformerAdder twtAdder = nbNetwork.getSubstation("subst")
                .newTwoWindingsTransformer()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU1(220.0)
                .setRatedU2(400.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(twtAdder)
                .withBusOrBusbarSectionId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs1")
                .withPositionOrder2(121)
                .withDirection2(TOP)
                .build();
        modification.apply(nbNetwork);
        roundTripXmlTest(nbNetwork, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-twt-feeders-bbs.xml");
    }

    @Test
    void baseBusBreakerTwoWindingsTransformerTest() throws IOException {
        TwoWindingsTransformerAdder twtAdder = bbNetwork.getSubstation("P1")
                .newTwoWindingsTransformer()
                .setId("twtTest")
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU1(220.0)
                .setRatedU2(400.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(twtAdder)
                .withBusOrBusbarSectionId1("NGEN")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("NHV1")
                .withPositionOrder2(121)
                .withDirection2(TOP)
                .build();
        modification.apply(bbNetwork);
        roundTripXmlTest(bbNetwork, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-create-twt-feeder-bays.xml");
    }

    @Test
    void testWithoutExtension() {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        LineAdder lineAdder = network.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs1")
                .withPositionOrder2(121)
                .withDirection2(TOP)
                .build()
                .apply(network, true, Reporter.NO_OP);

        Line line = network.getLine("lineTest");
        assertNotNull(line);

        ConnectablePosition<Line> position = line.getExtension(ConnectablePosition.class);
        assertNull(position);
    }
}
