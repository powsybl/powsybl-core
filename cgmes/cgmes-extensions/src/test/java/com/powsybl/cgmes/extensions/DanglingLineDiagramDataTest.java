/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class DanglingLineDiagramDataTest extends AbstractLineDiagramDataTest {

    @Test
    public void test() {
        Network network = Networks.createNetworkWithDanglingLine();
        DanglingLine danglingLine = network.getDanglingLine("DanglingLine");

        LineDiagramData<DanglingLine> danglingLineDiagramData = LineDiagramData.getOrCreateDiagramData(danglingLine);
        assertNotNull(danglingLineDiagramData);

        danglingLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        danglingLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        danglingLine.addExtension(LineDiagramData.class, danglingLineDiagramData);

        DanglingLine danglingLine2 = network.getDanglingLine("DanglingLine");
        LineDiagramData<DanglingLine> danglingLineDiagramData2 = danglingLine2.getExtension(LineDiagramData.class);

        assertEquals(1, danglingLineDiagramData2.getDiagramsNames().size());
        checkDiagramData(danglingLineDiagramData2, DIAGRAM_NAME);
    }

    @Test
    public void testMultipleDiagrams() {
        Network network = Networks.createNetworkWithDanglingLine();
        DanglingLine danglingLine = network.getDanglingLine("DanglingLine");

        LineDiagramData<DanglingLine> danglingLineDiagramData = LineDiagramData.getOrCreateDiagramData(danglingLine);
        assertNotNull(danglingLineDiagramData);

        danglingLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        danglingLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        danglingLineDiagramData.addPoint(DIAGRAM2_NAME, new DiagramPoint(10, 20, 1));
        danglingLineDiagramData.addPoint(DIAGRAM2_NAME, new DiagramPoint(20, 10, 2));
        danglingLine.addExtension(LineDiagramData.class, danglingLineDiagramData);

        DanglingLine danglingLine2 = network.getDanglingLine("DanglingLine");
        LineDiagramData<DanglingLine> danglingLineDiagramData2 = danglingLine2.getExtension(LineDiagramData.class);

        assertEquals(2, danglingLineDiagramData2.getDiagramsNames().size());
        checkDiagramData(danglingLineDiagramData2, DIAGRAM_NAME);
        checkDiagramData(danglingLineDiagramData2, DIAGRAM2_NAME);
    }
}
