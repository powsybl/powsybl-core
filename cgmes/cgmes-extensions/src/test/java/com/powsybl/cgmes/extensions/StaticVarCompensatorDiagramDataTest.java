/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import org.junit.Test;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class StaticVarCompensatorDiagramDataTest extends AbstractInjectionDiagramDataTest {

    @Test
    public void test() {
        Network network = Networks.createNetworkWithStaticVarCompensator();
        StaticVarCompensator svc = network.getStaticVarCompensator("Svc");

        InjectionDiagramData<StaticVarCompensator> svcDiagramData = new InjectionDiagramData<>(svc);
        InjectionDiagramData.InjectionDiagramDetails diagramDetails = svcDiagramData.new InjectionDiagramDetails(new DiagramPoint(20, 10, 0), 90);
        diagramDetails.addTerminalPoint(new DiagramPoint(15, 10, 2));
        diagramDetails.addTerminalPoint(new DiagramPoint(0, 10, 1));
        svcDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        svc.addExtension(InjectionDiagramData.class, svcDiagramData);

        StaticVarCompensator svc2 = network.getStaticVarCompensator("Svc");
        InjectionDiagramData<StaticVarCompensator> svcDiagramData2 = svc2.getExtension(InjectionDiagramData.class);

        checkDiagramData(svcDiagramData2, DIAGRAM_NAME);
    }

}
