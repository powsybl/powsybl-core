/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import java.io.IOException;

import org.joda.time.DateTime;
import org.junit.Test;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevel.NodeBreakerView;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class NodeBreakerInternalConnectionsTest extends AbstractConverterTest {

    @Test
    public void roundTripTest() throws IOException {
        roundTripXmlTest(
                networkWithInternalConnections(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/internalConnections.xiidm");
    }

    private Network networkWithInternalConnections() {
        Network network = NetworkFactory
                .create("internal-connections", "test")
                .setCaseDate(DateTime.parse("2018-11-08T12:33:26.208+01:00"));

        Substation s1 = network.newSubstation()
                .setId("s1")
                .setCountry(Country.ES)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        NodeBreakerView n1 = vl1.getNodeBreakerView();
        n1.setNodeCount(5);
        vl1.newGenerator()
                .setId("g1")
                .setNode(0)
                .setMinP(0)
                .setMaxP(100)
                .setTargetP(10)
                .setVoltageRegulatorOn(true)
                .setTargetV(400)
                .add();
        n1.newInternalConnection().setNode1(0).setNode2(1).add();
        n1.newSwitch().setId("br1").setNode1(1).setNode2(2).setKind(SwitchKind.BREAKER).add();
        n1.newBusbarSection().setId("b1").setNode(2).add();
        n1.newInternalConnection().setNode1(3).setNode2(4).add();

        Substation s2 = network.newSubstation()
                .setId("s2")
                .setCountry(Country.ES)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        NodeBreakerView n2 = vl2.getNodeBreakerView();
        n2.setNodeCount(5);
        vl2.newLoad()
                .setId("l2")
                .setNode(0)
                .setP0(10)
                .setQ0(1)
                .add();
        n2.newInternalConnection().setNode1(0).setNode2(1).add();
        n2.newSwitch().setId("br2").setNode1(1).setNode2(2).setKind(SwitchKind.BREAKER).add();
        n2.newBusbarSection().setId("b2").setNode(2).add();
        n2.newInternalConnection().setNode1(3).setNode2(4).add();

        network.newLine()
                .setId("line1-2")
                .setVoltageLevel1("vl1")
                .setNode1(4)
                .setVoltageLevel2("vl2")
                .setNode2(4)
                .setR(0.1)
                .setX(10)
                .setG1(0)
                .setB1(0)
                .setG2(0)
                .setB2(0)
                .add();
        return network;
    }
}
