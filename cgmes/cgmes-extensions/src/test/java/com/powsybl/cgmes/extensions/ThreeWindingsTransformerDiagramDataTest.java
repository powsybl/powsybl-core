/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ThreeWindingsTransformerDiagramDataTest {

    public static String DIAGRAM_NAME = "default";

    @Test
    public void test() {
        Network network = Networks.createNetworkWithThreeWindingsTransformer();
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("Transformer3w");

        ThreeWindingsTransformerDiagramData twtDiagramData = new ThreeWindingsTransformerDiagramData(twt);
        ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails diagramDetails = twtDiagramData.new ThreeWindingsTransformerDiagramDataDetails(new DiagramPoint(20, 13, 0), 90);

        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(15, 10, 2));
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(0, 10, 1));
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(25, 10, 1));
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(40, 10, 2));
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL3, new DiagramPoint(20, 16, 1));
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL3, new DiagramPoint(20, 30, 2));

        twtDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        twt.addExtension(ThreeWindingsTransformerDiagramData.class, twtDiagramData);
        assertTrue(twtDiagramData.getDiagramsNames().size() > 0);

        ThreeWindingsTransformer twt2 = network.getThreeWindingsTransformer("Transformer3w");
        ThreeWindingsTransformerDiagramData twtDiagramData2 = twt2.getExtension(ThreeWindingsTransformerDiagramData.class);
        assertNotNull(twtDiagramData2);

        ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails diagramDataDetails2 = twtDiagramData2.getData(DIAGRAM_NAME);
        assertNotNull(diagramDataDetails2);

        assertEquals(0, diagramDataDetails2.getPoint().getSeq(), 0);
        assertEquals(20, diagramDataDetails2.getPoint().getX(), 0);
        assertEquals(13, diagramDataDetails2.getPoint().getY(), 0);
        assertEquals(90, diagramDataDetails2.getRotation(), 0);
        assertEquals(1, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL1).get(0).getSeq(), 0);
        assertEquals(0, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL1).get(0).getX(), 0);
        assertEquals(10, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL1).get(0).getY(), 0);
        assertEquals(2, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL1).get(1).getSeq(), 0);
        assertEquals(15, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL1).get(1).getX(), 0);
        assertEquals(10, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL1).get(1).getY(), 0);
        assertEquals(1, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL2).get(0).getSeq(), 0);
        assertEquals(25, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL2).get(0).getX(), 0);
        assertEquals(10, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL2).get(0).getY(), 0);
        assertEquals(2, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL2).get(1).getSeq(), 0);
        assertEquals(40, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL2).get(1).getX(), 0);
        assertEquals(10, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL2).get(1).getY(), 0);
        assertEquals(1, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL3).get(0).getSeq(), 0);
        assertEquals(20, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL3).get(0).getX(), 0);
        assertEquals(16, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL3).get(0).getY(), 0);
        assertEquals(2, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL3).get(1).getSeq(), 0);
        assertEquals(20, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL3).get(1).getX(), 0);
        assertEquals(30, diagramDataDetails2.getTerminalPoints(DiagramTerminal.TERMINAL3).get(1).getY(), 0);
    }

}
