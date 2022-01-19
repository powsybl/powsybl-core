/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;

/**
 * Create a new Line from a given Line Adder and attach it on an existing Line by cutting the latter.
 *
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class AttachNewLineOnLineTest extends AbstractXmlConverterTest {

    @Test
    public void attachLineOnLineNbTest() throws IOException {
        Network network = createNbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new AttachNewLineOnLine("NHV1_NHV2_1_VL#0", "bbs", line, adder);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-split-nb-l.xml");
    }

    @Test
    public void attachLineOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new AttachNewLineOnLine("NHV1_NHV2_1_VL#0", "bus", line, adder);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-line-split-bb-l.xml");
    }
}
