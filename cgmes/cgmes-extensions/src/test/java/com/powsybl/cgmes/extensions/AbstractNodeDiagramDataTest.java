/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractNodeDiagramDataTest {
    protected static String DIAGRAM_NAME = "default";

    protected <T> void checkDiagramData(NodeDiagramData<?> diagramData, String diagramName) {
        assertNotNull(diagramData);
        NodeDiagramData.NodeDiagramDataDetails nodeDetails = diagramData.getData(diagramName);
        assertNotNull(nodeDetails);
        assertEquals(1, nodeDetails.getPoint1().getSeq(), 0);
        assertEquals(0, nodeDetails.getPoint1().getX(), 0);
        assertEquals(10, nodeDetails.getPoint1().getY(), 0);
        assertEquals(2, nodeDetails.getPoint2().getSeq(), 0);
        assertEquals(10, nodeDetails.getPoint2().getX(), 0);
        assertEquals(0, nodeDetails.getPoint2().getY(), 0);
    }

}
