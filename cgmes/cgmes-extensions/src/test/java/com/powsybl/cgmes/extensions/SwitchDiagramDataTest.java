/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class SwitchDiagramDataTest extends AbstractCouplingDeviceDiagramDataTest {

    @Test
    public void test() {
        Network network = Networks.createNetworkWithSwitch();
        Switch sw = network.getSwitch("Switch");

        CouplingDeviceDiagramData<Switch> switchDiagramData = new CouplingDeviceDiagramData<>(sw);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails diagramDetails = switchDiagramData.new CouplingDeviceDiagramDetails(new DiagramPoint(20, 10, 0), 90);
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(15, 10, 2));
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(0, 10, 1));
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(25, 10, 1));
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(40, 10, 2));
        switchDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        sw.addExtension(CouplingDeviceDiagramData.class, switchDiagramData);
        assertTrue(switchDiagramData.getDiagramsNames().size() > 0);

        Switch sw2 = network.getSwitch("Switch");
        CouplingDeviceDiagramData<Switch> switchDiagramData2 = sw2.getExtension(CouplingDeviceDiagramData.class);

        checkDiagramData(switchDiagramData2, DIAGRAM_NAME);
    }

}
