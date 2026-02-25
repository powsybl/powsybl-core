/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesMetadataModelsTest {

    @Test
    void test() {
        Network network = NetworkTest1Factory.create();
        network.newExtension(CgmesMetadataModelsAdder.class)
            .newModel()
                .setSubset(CgmesSubset.EQUIPMENT)
                .setModelingAuthoritySet("http://powsybl.org")
                .addProfile("http://equipment-core")
                .add()
            .newModel()
                .setSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS)
                .setId("sshId")
                .setDescription("SSH description")
                .setModelingAuthoritySet("http://powsybl.org")
                .setVersion(1)
                .addProfile("http://steady-state-hypothesis")
                .addDependentOn("ssh-dependency1")
                .addDependentOn("ssh-dependency2")
                .add()
            .newModel()
                .setSubset(CgmesSubset.STATE_VARIABLES)
                .setId("svId")
                .setDescription("SV description")
                .setModelingAuthoritySet("http://powsybl.org")
                .setVersion(2)
                .addProfile("http://state-variables")
                .addDependentOn("sv-dependency")
                .add()
            .add();

        CgmesMetadataModels extension = network.getExtension(CgmesMetadataModels.class);
        assertNotNull(extension);

        CgmesMetadataModel eq = extension.getModelForSubset(CgmesSubset.EQUIPMENT).orElseThrow();
        assertNull(eq.getId());
        assertNull(eq.getDescription());
        assertEquals("http://powsybl.org", eq.getModelingAuthoritySet());
        assertEquals(0, eq.getVersion());
        assertTrue(eq.getDependentOn().isEmpty());
        assertTrue(eq.getSupersedes().isEmpty());

        CgmesMetadataModel ssh = extension.getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS).orElseThrow();
        assertEquals("SSH description", ssh.getDescription());
        assertEquals("http://powsybl.org", ssh.getModelingAuthoritySet());
        assertEquals(1, ssh.getVersion());
        assertEquals(Set.of("ssh-dependency1", "ssh-dependency2"), ssh.getDependentOn());

        CgmesMetadataModel sv = extension.getModelForSubset(CgmesSubset.STATE_VARIABLES).orElseThrow();
        assertEquals("SV description", sv.getDescription());
        assertEquals("http://powsybl.org", sv.getModelingAuthoritySet());
        assertEquals(2, sv.getVersion());
        assertEquals(Set.of("sv-dependency"), sv.getDependentOn());
    }

    @Test
    void invalid() {
        Network network = NetworkTest1Factory.create();
        CgmesMetadataModelsAdder adder = network.newExtension(CgmesMetadataModelsAdder.class);
        // Expected an exception because the list of models is empty
        assertThrows(PowsyblException.class, adder::add);
    }

    @Test
    void testAnonymizedCgmesMetadataModels() throws IOException {
        //Given
        // Id, ModelingAuthoritySet, DependentOn, Supersedes
        Network network = NetworkTest1Factory.create();
        network.newExtension(CgmesMetadataModelsAdder.class)
                .newModel()
                .setSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS)
                .setId("sshId")
                .setDescription("SSH description")
                .setModelingAuthoritySet("RTE")
                .setVersion(1)
                .addProfile("http://steady-state-hypothesis")
                .addDependentOn("ssh-dependency1")
                .addDependentOn("ssh-dependency2")
                .addSupersedes("AA SSH previous ID")
                .add()
                .add();
        CgmesMetadataModels extension = network.getExtension(CgmesMetadataModels.class);
        assertNotNull(extension);
        // When Export (with anonymized option)
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Anonymizer anonymizer = NetworkSerDe.write(network, new ExportOptions().setAnonymized(true), os);
        String anonymizedSSHId = anonymizer.anonymizeString("sshId");
        String anonymizedModelingAuthoritySet = anonymizer.anonymizeString("RTE");
        String anonymizedDependentOn1 = anonymizer.anonymizeString("ssh-dependency1");
        String anonymizedDependentOn2 = anonymizer.anonymizeString("ssh-dependency2");
        String anonymizedSupersedes = anonymizer.anonymizeString("AA SSH previous ID");
        // Then check xml content (contain only anonymized values)
        byte[] anonymizedXml = os.toByteArray();
        String xmlContent = new String(anonymizedXml, StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("id=\"" + anonymizedSSHId + "\""));
        assertFalse(xmlContent.contains("id=\"sshId\""));
        assertTrue(xmlContent.contains("modelingAuthoritySet=\"" + anonymizedModelingAuthoritySet + "\""));
        assertFalse(xmlContent.contains("modelingAuthoritySet=\"RTE\""));
        assertTrue(xmlContent.contains("dependentOnModel>" + anonymizedDependentOn1 + "<"));
        assertFalse(xmlContent.contains("dependentOnModel>ssh-dependency1<"));
        assertTrue(xmlContent.contains("dependentOnModel>" + anonymizedDependentOn2 + "<"));
        assertFalse(xmlContent.contains("dependentOnModel>ssh-dependency2<"));
        assertTrue(xmlContent.contains("supersedesModel>" + anonymizedSupersedes + "<"));
        assertFalse(xmlContent.contains("AA SSH previous ID"));
        //Then import without anonymizer
        try (ByteArrayInputStream is = new ByteArrayInputStream(anonymizedXml)) {
            Network importedNetwork = NetworkSerDe.read(is);
            CgmesMetadataModels importedCgmesMetadataModels = importedNetwork.getExtension(CgmesMetadataModels.class);
            assertEquals(anonymizedSSHId, importedCgmesMetadataModels.getModels().iterator().next().getId());
            assertEquals(anonymizedModelingAuthoritySet, importedCgmesMetadataModels.getModels().iterator().next().getModelingAuthoritySet());
            assertEquals(Set.of(anonymizedDependentOn1, anonymizedDependentOn2),
                    importedCgmesMetadataModels.getModels().iterator().next().getDependentOn());
            assertEquals(Set.of(anonymizedSupersedes), importedCgmesMetadataModels.getModels().iterator().next().getSupersedes());
        }
    }
}
