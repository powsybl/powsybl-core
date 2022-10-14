/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology.data;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.modification.topology.CreateFeederBay;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static com.powsybl.commons.ComparisonUtils.compareTxt;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateGeneratorBayDataTest extends AbstractConverterTest {

    @Test
    public void testSerialize() throws IOException {
        CreateGeneratorBayData data = new CreateGeneratorBayData()
                .setGeneratorId("testG")
                .setGeneratorName("testGN")
                .setGeneratorFictitious(true)
                .setGeneratorMinP(0.0)
                .setGeneratorMaxP(100.0)
                .setGeneratorRegulatingConnectableId("twt")
                .setGeneratorRegulatingSide("TWO")
                .setGeneratorVoltageRegulatorOn(true)
                .setGeneratorTargetV(225.0)
                .setGeneratorTargetP(0.0)
                .setGeneratorTargetQ(0.0)
                .setBusbarSectionId("testBBS")
                .setPositionOrder(10);
        roundTripTest(data, CreateGeneratorBayData::write, path -> {
            CreateGeneratorBayData d = new CreateGeneratorBayData();
            d.update(path);
            return d;
        }, "/data/createGeneratorBayData.json");
    }

    @Test
    public void testCopy() throws IOException {
        CreateGeneratorBayData data = new CreateGeneratorBayData()
                .setGeneratorId("testG")
                .setGeneratorName("testGN")
                .setGeneratorFictitious(true)
                .setGeneratorMinP(0.0)
                .setGeneratorMaxP(100.0)
                .setGeneratorRegulatingConnectableId("twt")
                .setGeneratorRegulatingSide("TWO")
                .setGeneratorVoltageRegulatorOn(true)
                .setGeneratorTargetV(225.0)
                .setGeneratorTargetP(0.0)
                .setGeneratorTargetQ(0.0)
                .setBusbarSectionId("testBBS")
                .setPositionOrder(10);
        CreateGeneratorBayData data1 = new CreateGeneratorBayData();
        data1.copy(data);
        data1.write(tmpDir.resolve("/test.json"));
        try (InputStream is = Files.newInputStream(tmpDir.resolve("/test.json"))) {
            compareTxt(getClass().getResourceAsStream("/data/createGeneratorBayData.json"), is);
        }
    }

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

        PowsyblException e = assertThrows(PowsyblException.class, data::toModification);
        assertEquals("A network should be passed in parameters to create the adder", e.getMessage());

        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        CreateFeederBay modification = data.toModification(network);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-generator-bbs1.xml");
    }
}
