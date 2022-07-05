/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BusbarDiagramDataTest extends AbstractNodeDiagramDataTest {

    @Test
    public void test() {
        Network network = Networks.createNetworkWithBusbar();
        BusbarSection busbar = network.getVoltageLevel("VoltageLevel").getNodeBreakerView().getBusbarSection("Busbar");

        NodeDiagramData<BusbarSection> busbarDiagramData = NodeDiagramData.getOrCreateDiagramData(busbar);
        assertNotNull(busbarDiagramData);
        NodeDiagramData.NodeDiagramDataDetails nodeDetails = busbarDiagramData.new NodeDiagramDataDetails();
        nodeDetails.setPoint2(new DiagramPoint(10, 0, 2));
        nodeDetails.setPoint1(new DiagramPoint(0, 10, 1));
        busbarDiagramData.addData(DIAGRAM_NAME, nodeDetails);
        busbar.addExtension(NodeDiagramData.class, busbarDiagramData);

        BusbarSection busbar2 = network.getVoltageLevel("VoltageLevel").getNodeBreakerView().getBusbarSection("Busbar");
        NodeDiagramData<BusbarSection> busbarDiagramData2 = busbar2.getExtension(NodeDiagramData.class);

        checkDiagramData(busbarDiagramData2, DIAGRAM_NAME);
    }

}
