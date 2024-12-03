/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

class CondenserImportExportTest extends AbstractSerDeTest {

    @Test
    void cgmes2condenserConversionTest() {
        Network network = Network.read("condenser2_EQ.xml", getClass().getResourceAsStream("/issues/condenser2_EQ.xml"));
        assertEquals(1, network.getGeneratorCount());
        Generator g = network.getGenerators().iterator().next();
        assertEquals(0, g.getMinP());
        assertEquals(0, g.getMaxP());
        assertTrue(g.isCondenser());
    }

    @Test
    void cgmes2condenserExportTest() throws IOException {
        Network network = Network.read("condenser2_EQ.xml", getClass().getResourceAsStream("/issues/condenser2_EQ.xml"));
        String eq = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDir);
        // No generating unit is referred, no generating unit is defined
        assertFalse(eq.contains("cim:RotatingMachine.GeneratingUnit rdf:resource="));
        assertFalse(eq.contains("cim:GeneratingUnit rdf:ID"));
        assertTrue(eq.contains("cim:SynchronousMachine.type rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#SynchronousMachineKind.condenser"));
    }

    @Test
    void cgmes3condenserConversionTest() {
        Network network = Network.read("condenser3_EQ.xml", getClass().getResourceAsStream("/issues/condenser3_EQ.xml"));
        assertEquals(1, network.getGeneratorCount());
        Generator g = network.getGenerators().iterator().next();
        assertEquals(0, g.getMinP());
        assertEquals(0, g.getMaxP());
        assertTrue(g.isCondenser());
    }

    @Test
    void cgmes3condenserExportTest() throws IOException {
        Network network = Network.read("condenser3_EQ.xml", getClass().getResourceAsStream("/issues/condenser3_EQ.xml"));
        String eq = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDir);
        // No generating unit is referred, no generating unit is defined
        assertFalse(eq.contains("cim:RotatingMachine.GeneratingUnit rdf:resource="));
        assertFalse(eq.contains("cim:GeneratingUnit rdf:ID"));
        assertTrue(eq.contains("cim:SynchronousMachine.type rdf:resource=\"http://iec.ch/TC57/CIM100#SynchronousMachineKind.condenser"));
    }

    @Test
    void cgmes3condenserReferencePriorityTest() throws IOException {
        Network network = Network.read(new ResourceDataSource("condenser3",
                new ResourceSet("/issues", "condenser3_EQ.xml", "condenser3_SSH.xml")));
        Generator g = network.getGenerator("Condenser1");
        ReferencePriority referencePriority = ReferencePriorities.get(network).iterator().next();
        assertNotNull(referencePriority);
        assertEquals(g.getTerminal(), referencePriority.getTerminal());

        String eq = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDir);
        // No generating unit is referred, no generating unit is defined
        assertFalse(eq.contains("cim:RotatingMachine.GeneratingUnit rdf:resource="));
        assertFalse(eq.contains("cim:GeneratingUnit rdf:ID"));
        assertTrue(eq.contains("cim:SynchronousMachine.type rdf:resource=\"http://iec.ch/TC57/CIM100#SynchronousMachineKind.condenser"));
        String ssh = ConversionUtil.writeCgmesProfile(network, "SSH", tmpDir);
        assertTrue(ssh.contains("cim:SynchronousMachine.referencePriority>1<"));
    }
}
