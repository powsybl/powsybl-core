/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractVoltageLevelExportTest extends AbstractSerDeTest {

    @Test
    public void nodeBreakerTest() throws IOException {
        Network network = FictitiousSwitchFactory.create();
        try (StringWriter writer = new StringWriter()) {
            network.getVoltageLevel("C").exportTopology(writer, new Random(0));
            writer.flush();
            // as Graphviz builder library do not have to stable export (order of nodes and edges can change at each run)
            // we only compare unsorted lines
            List<String> linesRef = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/fictitious-switch-c.dot"))).lines().collect(Collectors.toList());
            List<String> lines = Arrays.asList(writer.toString().split("[\\r\\n]+"));
            assertTrue(lines.containsAll(linesRef));
        }
    }

    @Test
    public void busBreakerTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        try (StringWriter writer = new StringWriter()) {
            network.getVoltageLevel("VLHV1").exportTopology(writer, new Random(0));
            writer.flush();
            // as Graphviz builder library do not have to stable export (order of nodes and edges can change at each run)
            // we only compare unsorted lines
            List<String> linesRef = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/eurostag-tutorial-example1-vlhv1.dot"))).lines().collect(Collectors.toList());
            List<String> lines = Arrays.asList(writer.toString().split("[\\r\\n]+"));
            assertTrue(lines.containsAll(linesRef));
        }
    }
}
