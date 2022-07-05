/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import org.junit.Test;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class HvdcLineDiagramDataTest extends AbstractLineDiagramDataTest {

    @Test
    public void test() {
        Network network = Networks.createNetworkWithHvdcLine();
        HvdcLine hvdcLine = network.getHvdcLine("HvdcLine");

        LineDiagramData<HvdcLine> hvdcLineDiagramData = new LineDiagramData<>(hvdcLine);
        hvdcLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        hvdcLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        hvdcLine.addExtension(LineDiagramData.class, hvdcLineDiagramData);

        HvdcLine hvdcLine2 = network.getHvdcLine("HvdcLine");
        LineDiagramData<HvdcLine> hvdcLineDiagramData2 = hvdcLine2.getExtension(LineDiagramData.class);

        assertEquals(1, hvdcLineDiagramData2.getDiagramsNames().size());
        checkDiagramData(hvdcLineDiagramData2, DIAGRAM_NAME);
    }

    @Test
    public void testMultipleDiagrams() {
        Network network = Networks.createNetworkWithHvdcLine();
        HvdcLine hvdcLine = network.getHvdcLine("HvdcLine");

        LineDiagramData<HvdcLine> hvdcLineDiagramData = new LineDiagramData<>(hvdcLine);
        hvdcLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(10, 0, 2));
        hvdcLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(0, 10, 1));
        hvdcLineDiagramData.addPoint(DIAGRAM2_NAME, new DiagramPoint(10, 20, 1));
        hvdcLineDiagramData.addPoint(DIAGRAM2_NAME, new DiagramPoint(20, 10, 2));
        hvdcLine.addExtension(LineDiagramData.class, hvdcLineDiagramData);

        HvdcLine hvdcLine2 = network.getHvdcLine("HvdcLine");
        LineDiagramData<HvdcLine> hvdcLineDiagramData2 = hvdcLine2.getExtension(LineDiagramData.class);

        assertEquals(2, hvdcLineDiagramData2.getDiagramsNames().size());
        checkDiagramData(hvdcLineDiagramData2, DIAGRAM_NAME);
        checkDiagramData(hvdcLineDiagramData2, DIAGRAM2_NAME);
    }
}
