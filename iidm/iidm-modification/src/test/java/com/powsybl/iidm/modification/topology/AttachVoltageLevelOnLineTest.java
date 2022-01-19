/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createBbNetwork;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class AttachVoltageLevelOnLineTest extends AbstractXmlConverterTest {

    @Test
    public void attachVoltageLevelOnLineNbTest() throws IOException {
        Network network = createNbNetwork();
        NetworkModification modification = new AttachVoltageLevelOnLine("NHV1_NHV2_1_VL#0", "bbs",
                network.getLine("NHV1_NHV2_1"));
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-split-nb-vl.xml");
    }

    @Test
    public void attachVoltageLevelOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        NetworkModification modification = new AttachVoltageLevelOnLine("NHV1_NHV2_1_VL#0", "bus",
                network.getLine("NHV1_NHV2_1"));
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-split-bb-vl.xml");
    }

}
