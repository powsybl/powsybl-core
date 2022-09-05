/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.NetworkXml;
import org.apache.commons.lang3.Range;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsAfter;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsBefore;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.TOP;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThrows;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateBranchFeedersTest extends AbstractXmlConverterTest {

    private Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

    @Test
    public void baseLineTest() throws IOException {
        LineAdder lineAdder = network.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        NetworkModification modification = new CreateBranchFeedersBuilder()
                .withBranchAdder(lineAdder)
                .withBbsId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBbsId2("bbs1")
                .withPositionOrder2(121)
                .withDirection2(TOP)
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-line-feeders-bbs.xml");
    }

    @Test
    public void getUnusedOrderPositionAfter() {
        LineAdder lineAdder = network.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        Optional<Range<Integer>> unusedOrderPositionsAfter = getUnusedOrderPositionsAfter(
                network.getVoltageLevel("vl1"), network.getBusbarSection("bbs2"));
        assertTrue(unusedOrderPositionsAfter.isPresent());
        assertEquals(121, (int) unusedOrderPositionsAfter.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter.get().getMaximum());

        int positionOrder1 = unusedOrderPositionsAfter.get().getMinimum();
        NetworkModification modification = new CreateBranchFeedersBuilder()
                .withBranchAdder(lineAdder)
                .withBbsId1("bbs1")
                .withPositionOrder1(positionOrder1)
                .withDirection1(TOP)
                .withBbsId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        modification.apply(network);

        ConnectablePosition<Line> newLine = network.getLine("lineTest").getExtension(ConnectablePosition.class);
        assertEquals(TOP, newLine.getFeeder1().getDirection());
        assertEquals(Optional.of(121), newLine.getFeeder1().getOrder());
    }

    @Test
    public void getUnusedOrderPositionBefore() {
        LineAdder lineAdder = network.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        Optional<Range<Integer>> unusedOrderPositionsBefore = getUnusedOrderPositionsBefore(
                network.getVoltageLevel("vl1"), network.getBusbarSection("bbs2"));
        assertTrue(unusedOrderPositionsBefore.isPresent());
        assertEquals(71, (int) unusedOrderPositionsBefore.get().getMinimum());
        assertEquals(79, (int) unusedOrderPositionsBefore.get().getMaximum());
        int positionOrder1 = unusedOrderPositionsBefore.get().getMaximum();

        NetworkModification modification = new CreateBranchFeedersBuilder()
                .withBranchAdder(lineAdder)
                .withBbsId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBbsId2("bbs2")
                .withPositionOrder2(positionOrder1)
                .withDirection2(BOTTOM)
                .build();
        modification.apply(network);

        ConnectablePosition<Line> newLine = network.getLine("lineTest").getExtension(ConnectablePosition.class);
        assertEquals(BOTTOM, newLine.getFeeder2().getDirection());
        assertEquals(Optional.of(79), newLine.getFeeder2().getOrder());
    }

    @Test
    public void testException() {
        LineAdder lineAdder = network.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);

        //wrong network
        Network network1 = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        CreateBranchFeeders modification0 = new CreateBranchFeedersBuilder().
                withBranchAdder(lineAdder)
                .withBbsId1("bbs1")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBbsId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> modification0.apply(network1, true, Reporter.NO_OP));
        assertEquals("Network given in parameters and in injectionAdder are different. Injection was added then removed", e0.getMessage());

        //wrong bbsId
        CreateBranchFeeders modification1 = new CreateBranchFeedersBuilder().
                withBranchAdder(lineAdder)
                .withBbsId1("bbs")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBbsId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> modification1.apply(network, true, Reporter.NO_OP));
        assertEquals("Busbar section bbs not found.", e1.getMessage());

        //wrong injectionPositionOrder
        CreateBranchFeeders modification2 = new CreateBranchFeedersBuilder().
                withBranchAdder(lineAdder)
                .withBbsId1("bbs1")
                .withPositionOrder1(0)
                .withDirection1(BOTTOM)
                .withBbsId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification2.apply(network, true, Reporter.NO_OP));
        assertEquals("PositionOrder 0 already taken.", e2.getMessage());
    }

    @Test
    public void baseTwoWindingsTransformerTest() throws IOException {
        TwoWindingsTransformerAdder twtAdder = network.getSubstation("subst")
                .newTwoWindingsTransformer()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU1(220.0)
                .setRatedU2(400.0);
        NetworkModification modification = new CreateBranchFeedersBuilder()
                .withBranchAdder(twtAdder)
                .withBbsId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBbsId2("bbs1")
                .withPositionOrder2(121)
                .withDirection2(TOP)
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-twt-feeders-bbs.xml");
    }

    @Test
    public void testWithoutExtension() {
        network = Importers.loadNetwork("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        LineAdder lineAdder = network.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        new CreateBranchFeedersBuilder()
                .withBranchAdder(lineAdder)
                .withBbsId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBbsId2("bbs1")
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
