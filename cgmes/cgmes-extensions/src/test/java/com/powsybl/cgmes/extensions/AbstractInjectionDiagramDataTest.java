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
public abstract class AbstractInjectionDiagramDataTest {
    protected static String DIAGRAM_NAME = "default";

    protected <T> void checkDiagramData(InjectionDiagramData<?> diagramData, String diagramName) {
        assertNotNull(diagramData);
        InjectionDiagramData.InjectionDiagramDetails diagramDataDetails = diagramData.getData(diagramName);
        assertEquals(0, diagramDataDetails.getPoint().getSeq(), 0);
        assertEquals(20, diagramDataDetails.getPoint().getX(), 0);
        assertEquals(10, diagramDataDetails.getPoint().getY(), 0);
        assertEquals(90, diagramDataDetails.getRotation(), 0);
        List<DiagramPoint> points = diagramDataDetails.getTerminalPoints();
        assertEquals(1, points.get(0).getSeq(), 0);
        assertEquals(0, points.get(0).getX(), 0);
        assertEquals(10, points.get(0).getY(), 0);
        assertEquals(2, points.get(1).getSeq(), 0);
        assertEquals(15, points.get(1).getX(), 0);
        assertEquals(10, points.get(1).getY(), 0);
    }

}
