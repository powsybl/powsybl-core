/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class VoltageLevelDiagramDataTest {

    protected static String DIAGRAM_NAME = "diagram";
    protected static String DIAGRAM_NAME2 = "diagram2";

    @Test
    public void test() {
        Network network = Networks.createNetworkWithGenerator();
        VoltageLevel vl = network.getVoltageLevels().iterator().next();
        assertFalse(VoltageLevelDiagramData.checkDiagramData(vl));
        assertNull(VoltageLevelDiagramData.getInternalNodeDiagramPoint(vl, DIAGRAM_NAME, 1));
        assertTrue(VoltageLevelDiagramData.getInternalNodeDiagramPoints(vl, DIAGRAM_NAME).length == 0);

        int[] nodes = {1, 2};
        DiagramPoint point1 = new DiagramPoint(1, 1, 1);
        DiagramPoint point2 = new DiagramPoint(2, 2, 2);
        VoltageLevelDiagramData.addInternalNodeDiagramPoint(vl, DIAGRAM_NAME, nodes[0], point1);
        VoltageLevelDiagramData.addInternalNodeDiagramPoint(vl, DIAGRAM_NAME, nodes[1], point2);

        assertEquals(VoltageLevelDiagramData.getInternalNodeDiagramPoint(vl, DIAGRAM_NAME, nodes[0]), point1);
        assertEquals(VoltageLevelDiagramData.getInternalNodeDiagramPoint(vl, DIAGRAM_NAME, nodes[1]), point2);

        assertTrue(nodes.length == 2);
        assertTrue(Arrays.equals(nodes, VoltageLevelDiagramData.getInternalNodeDiagramPoints(vl, DIAGRAM_NAME)));

        assertNull(VoltageLevelDiagramData.getInternalNodeDiagramPoint(vl, DIAGRAM_NAME2, 1));
    }

}
