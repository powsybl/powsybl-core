/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.google.common.io.ByteStreams;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Ground;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

class GroundConversionTest extends AbstractSerDeTest {

    @Test
    void groundConversionTest() {
        Network network = Network.read("groundTest.xml", getClass().getResourceAsStream("/groundTest.xml"));

        assertEquals(2, network.getGroundCount());

        Ground groundOU = network.getGround("OU");
        assertNotNull(groundOU);
        assertEquals("KD", groundOU.getNameOrId());
        assertNotNull(groundOU.getTerminal());
        assertFalse(groundOU.getTerminal().isConnected());
        assertEquals("S", groundOU.getTerminal().getVoltageLevel().getId());

        Ground groundCV = network.getGround("CV");
        assertNotNull(groundCV);
        assertEquals("CW", groundCV.getNameOrId());
        assertNotNull(groundCV.getTerminal());
        assertTrue(groundCV.getTerminal().isConnected());
        assertEquals("S", groundCV.getTerminal().getVoltageLevel().getId());
    }

    @Test
    void groundConversionRemoveTest() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.POST_PROCESSORS, "RemoveGrounds");
        importParams.put(CgmesImport.CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE, "NEVER");
        Network network = Network.read(
                new ResourceDataSource("groundTest.xml", new ResourceSet("/", "groundTest.xml")),
                importParams);

        assertEquals(0, network.getGroundCount());

        // Check also the exported GraphViz
        // Some edges have been removed, ensure it is exported properly
        StringWriter writer = new StringWriter();
        network.getVoltageLevel("S").exportTopology(writer, new Random(1));
        String actual = graphVizClean(writer.toString());

        String expected = new String(ByteStreams.toByteArray(Objects.requireNonNull(
                getClass().getResourceAsStream("/groundConversionRemoveGraph.dot"))), StandardCharsets.UTF_8);
        ComparisonUtils.assertTxtEquals(expected, actual);
    }

    private String graphVizClean(String gv) {
        // Remove all comments
        return gv.replaceAll("([\\n\\r])(\\s+)// scope.*([\\n\\r])", "$1$2// scope xxxxx$3");
    }
}
