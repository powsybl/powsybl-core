/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.GraphvizConnectivity;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GraphvizConnectivityTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException, NoSuchAlgorithmException {
        Network network = EurostagTutorialExample1Factory.create();
        try (StringWriter writer = new StringWriter()) {
            new GraphvizConnectivity(network, new Random(0)).write(writer);
            writer.flush();
            // as Graphviz builder library do not have to stable export (order of nodes and edges can change at any run)
            // we only compare unsorted lines
            List<String> linesRef = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/eurostag-tutorial-example1.dot"))).lines().collect(Collectors.toList());
            List<String> lines = Arrays.asList(writer.toString().split(System.lineSeparator()));
            assertTrue(lines.containsAll(linesRef));
        }
    }
}
