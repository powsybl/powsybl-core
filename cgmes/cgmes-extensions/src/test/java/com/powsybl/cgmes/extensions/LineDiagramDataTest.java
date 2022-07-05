/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LineDiagramDataTest extends AbstractLineDiagramDataTest {

    protected static final String DIAGRAM2_NAME = "diagram2";

    @Test
    public void test() {
        Network network = Networks.createNetworkWithLine();
        Line line = network.getLine("Line");

        LineDiagramData<Line> lineDiagramData = LineDiagramData.getOrCreateDiagramData(line);
        assertNotNull(lineDiagramData);
        lineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        lineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        line.addExtension(LineDiagramData.class, lineDiagramData);

        Line line2 = network.getLine("Line");
        LineDiagramData<Line> lineDiagramData2 = line2.getExtension(LineDiagramData.class);

        assertTrue(lineDiagramData2.getDiagramsNames().size() == 1);
        checkDiagramData(lineDiagramData2, DIAGRAM_NAME);
    }

    @Test
    public void testMultipleDiagrams() {
        Network network = Networks.createNetworkWithLine();
        Line line = network.getLine("Line");

        LineDiagramData<Line> lineDiagramData = LineDiagramData.getOrCreateDiagramData(line);
        assertNotNull(lineDiagramData);
        lineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        lineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));

        lineDiagramData.addPoint(DIAGRAM2_NAME, new DiagramPoint(10, 20, 1));
        lineDiagramData.addPoint(DIAGRAM2_NAME, new DiagramPoint(20, 10, 2));

        line.addExtension(LineDiagramData.class, lineDiagramData);

        Line line2 = network.getLine("Line");
        LineDiagramData<Line> lineDiagramData2 = line2.getExtension(LineDiagramData.class);

        assertEquals(2, lineDiagramData2.getDiagramsNames().size());
        checkDiagramData(lineDiagramData2, DIAGRAM_NAME);
        checkDiagramData(lineDiagramData2, DIAGRAM2_NAME);
    }

}
