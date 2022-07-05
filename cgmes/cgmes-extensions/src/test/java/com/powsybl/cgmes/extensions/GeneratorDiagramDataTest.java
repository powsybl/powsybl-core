/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class GeneratorDiagramDataTest extends AbstractInjectionDiagramDataTest {

    @Test
    public void test() {
        Network network = Networks.createNetworkWithGenerator();
        Generator generator = network.getGenerator("Generator");

        InjectionDiagramData<Generator> generatorDiagramData = new InjectionDiagramData<>(generator);
        InjectionDiagramData.InjectionDiagramDetails diagramDataDetails = generatorDiagramData.new InjectionDiagramDetails(new DiagramPoint(20, 10, 0), 90);
        diagramDataDetails.addTerminalPoint(new DiagramPoint(15, 10, 2));
        diagramDataDetails.addTerminalPoint(new DiagramPoint(0, 10, 1));
        generatorDiagramData.addData(DIAGRAM_NAME, diagramDataDetails);
        generator.addExtension(InjectionDiagramData.class, generatorDiagramData);
        assertTrue(generatorDiagramData.getDiagramsNames().size() > 0);

        Generator generator2 = network.getGenerator("Generator");
        InjectionDiagramData<Generator> generatorDiagramData2 = generator2.getExtension(InjectionDiagramData.class);

        checkDiagramData(generatorDiagramData2, DIAGRAM_NAME);

    }

}
