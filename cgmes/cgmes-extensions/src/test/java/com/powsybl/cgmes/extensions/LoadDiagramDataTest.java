/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import org.junit.Test;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LoadDiagramDataTest extends AbstractInjectionDiagramDataTest {

    @Test
    public void test() {
        Network network = Networks.createNetworkWithLoad();
        Load load = network.getLoad("Load");

        InjectionDiagramData<Load> loadDiagramData = new InjectionDiagramData<>(load);
        InjectionDiagramData.InjectionDiagramDetails diagramDetails = loadDiagramData.new InjectionDiagramDetails(new DiagramPoint(20, 10, 0), 90);
        diagramDetails.addTerminalPoint(new DiagramPoint(15, 10, 2));
        diagramDetails.addTerminalPoint(new DiagramPoint(0, 10, 1));
        loadDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        load.addExtension(InjectionDiagramData.class, loadDiagramData);

        Load load2 = network.getLoad("Load");
        InjectionDiagramData loadDiagramData2 = load2.getExtension(InjectionDiagramData.class);

        checkDiagramData(loadDiagramData2, DIAGRAM_NAME);
    }

}
