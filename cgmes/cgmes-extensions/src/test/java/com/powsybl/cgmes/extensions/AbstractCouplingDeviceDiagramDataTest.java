/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractCouplingDeviceDiagramDataTest {
    protected static String DIAGRAM_NAME = "default";

    protected <T> void checkDiagramData(CouplingDeviceDiagramData<?> diagramData, String diagramName) {
        assertNotNull(diagramData);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails dataDetails = diagramData.getData(diagramName);
        assertEquals(0, dataDetails.getPoint().getSeq(), 0);
        assertEquals(20, dataDetails.getPoint().getX(), 0);
        assertEquals(10, dataDetails.getPoint().getY(), 0);
        assertEquals(90, dataDetails.getRotation(), 0);
        List<DiagramPoint> t1Points = dataDetails.getTerminalPoints(DiagramTerminal.TERMINAL1);
        List<DiagramPoint> t2Points = dataDetails.getTerminalPoints(DiagramTerminal.TERMINAL2);
        assertEquals(1, t1Points.get(0).getSeq(), 0);
        assertEquals(0, t1Points.get(0).getX(), 0);
        assertEquals(10, t1Points.get(0).getY(), 0);
        assertEquals(2, t1Points.get(1).getSeq(), 0);
        assertEquals(15, t1Points.get(1).getX(), 0);
        assertEquals(10, t1Points.get(1).getY(), 0);
        assertEquals(1, t2Points.get(0).getSeq(), 0);
        assertEquals(25, t2Points.get(0).getX(), 0);
        assertEquals(10, t2Points.get(0).getY(), 0);
        assertEquals(2, t2Points.get(1).getSeq(), 0);
        assertEquals(40, t2Points.get(1).getX(), 0);
        assertEquals(10, t2Points.get(1).getY(), 0);
    }

}
