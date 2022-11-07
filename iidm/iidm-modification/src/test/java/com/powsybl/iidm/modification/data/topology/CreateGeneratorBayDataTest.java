/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.data.topology;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.modification.topology.CreateFeederBay;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateGeneratorBayDataTest extends AbstractConverterTest {

    @Test
    public void testToModification() throws IOException {
        CreateGeneratorBayData data = new CreateGeneratorBayData()
                .setGeneratorId("newGenerator")
                .setGeneratorVoltageRegulatorOn(true)
                .setGeneratorMaxP(9999)
                .setGeneratorMinP(-9999)
                .setGeneratorTargetV(25.5)
                .setGeneratorTargetP(600)
                .setGeneratorTargetQ(300)
                .setGeneratorRatedS(10)
                .setGeneratorEnergySource(EnergySource.NUCLEAR)
                .setBusbarSectionId("bbs1")
                .setPositionOrder(115)
                .setDirection(BOTTOM);

        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        CreateFeederBay modification = data.toModification(network);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-generator-bbs1.xml");
    }
}
