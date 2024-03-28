/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@SuppressWarnings("checkstyle:RegexpSingleline")
class CommonGridModelExportTest extends AbstractSerDeTest {

    private static final Pattern REGEX_ID = Pattern.compile("FullModel rdf:about=\"(.*?)\"");
    private static final Pattern REGEX_VERSION = Pattern.compile("Model.version>(.*?)<");
    private static final Pattern REGEX_DEPENDENT_ON = Pattern.compile("Model.DependentOn rdf:resource=\"(.*?)\"");
    private static final Pattern REGEX_SUPERSEDES = Pattern.compile("Model.Supersedes rdf:resource=\"(.*?)\"");
    private static final Pattern REGEX_MAS = Pattern.compile("Model.modelingAuthoritySet>(.*?)<");

    @Test
    void testCgmExport() throws IOException {
        /*
        Summary from CGM Building Process Implementation Guide:

        A CGM is created by assembling a set of IGMs for the same scenarioTime.
        A Merging Agent is responsible for building the CGM.

        The IGMs are first validated for plausibility by solving a power flow.
        The CGM is then assembled from IGMs and a power flow is solved,
        potentially adjusting some of the IGMs power flow hypothesis.

        The Merging Agent provides an updated SSH for each IGM,
        containing a md:Model.Supersedes reference to the IGM’s original SSH CIMXML file
        and a single SV for the whole CGM, containing the results of power flow calculation.
        The SV file must contain a md:Model.DependentOn reference to each IGM’s updated SSH.

        The power flow of a CGM is calculated without topology processing. No new TP CIMXML
        files are created. IGM TP files are used as an input. No TP fie is created as the result of CGM building.
        This means that the CGM SV file must contain a md:Model.DependentOn reference to each IGM’s original TP.
        */

        // Read the network (CGM) with its subnetworks (IGMs)
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource());
        assertEquals(2, network.getSubnetworks().size());

        // Expected values
        String beMas = "http://elia.be/CGMES/2.4.15";
        String nlMas = "http://tennet.nl/CGMES/2.4.15";
        String originalBeSshId = "urn:uuid:52b712d1-f3b0-4a59-9191-79f2fb1e4c4e";
        String originalNlSshId = "urn:uuid:66085ffe-dddf-4fc8-805c-2c7aa2097b90";
        String originalBeTpId = "urn:uuid:f2f43818-09c8-4252-9611-7af80c398d20";
        String originalNlTpId = "urn:uuid:5d32d257-1646-4906-a1f6-4d7ce3f91569";
        int originalVersion = 2;

        // Check the original IGMs SSH and TP content
        Network beNetwork = network.getSubnetwork("urn:uuid:d400c631-75a0-4c30-8aed-832b0d282e73");
        Network nlNetwork = network.getSubnetwork("urn:uuid:77b55f87-fc1e-4046-9599-6c6b4f991a86");

        CgmesMetadataModel originalBeSshModel = getSubsetModel(beNetwork, CgmesSubset.STEADY_STATE_HYPOTHESIS);
        assertEquals(originalBeSshId, originalBeSshModel.getId());
        assertEquals(originalVersion, originalBeSshModel.getVersion());
        assertEquals(beMas, originalBeSshModel.getModelingAuthoritySet());

        CgmesMetadataModel originalNlSshModel = getSubsetModel(nlNetwork, CgmesSubset.STEADY_STATE_HYPOTHESIS);
        assertEquals(originalNlSshId, originalNlSshModel.getId());
        assertEquals(originalVersion, originalNlSshModel.getVersion());
        assertEquals(nlMas, originalNlSshModel.getModelingAuthoritySet());

        CgmesMetadataModel originalBeTpModel = getSubsetModel(beNetwork, CgmesSubset.TOPOLOGY);
        assertEquals(originalBeTpId, originalBeTpModel.getId());
        assertEquals(originalVersion, originalBeTpModel.getVersion());
        assertEquals(beMas, originalBeTpModel.getModelingAuthoritySet());

        CgmesMetadataModel originalNlTpModel = getSubsetModel(nlNetwork, CgmesSubset.TOPOLOGY);
        assertEquals(originalNlTpId, originalNlTpModel.getId());
        assertEquals(originalVersion, originalNlTpModel.getVersion());
        assertEquals(nlMas, originalNlTpModel.getModelingAuthoritySet());

        // Perform a CGM export and read the exported files
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.CGM_EXPORT, true);
        String basename = "test-assembled";
        // network.write("CGMES", exportParams, Path.of(basename));  // TODO remove
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        String updatedBeSshXml = Files.readString(tmpDir.resolve(basename + "_BE_SSH.xml"));
        String updatedNlSshXml = Files.readString(tmpDir.resolve(basename + "_NL_SSH.xml"));
        String updatedCgmSvXml = Files.readString(tmpDir.resolve(basename + "_SV.xml"));

        // Each updated IGM SSH should supersede the original one
        assertEquals(originalBeSshId, getOccurrences(updatedBeSshXml, REGEX_SUPERSEDES).iterator().next());
        assertEquals(originalNlSshId, getOccurrences(updatedNlSshXml, REGEX_SUPERSEDES).iterator().next());

        // The updated CGM SV should depend on the updated IGMs SSH and the original IGMs TP
        String updatedBeSshId = getOccurrences(updatedBeSshXml, REGEX_ID).iterator().next();
        String updatedNlSshId = getOccurrences(updatedNlSshXml, REGEX_ID).iterator().next();
        Set<String> expectedDependencies = Set.of(updatedBeSshId, updatedNlSshId, originalBeTpId, originalNlTpId);
        assertEquals(expectedDependencies, getOccurrences(updatedCgmSvXml, REGEX_DEPENDENT_ON));

        // Check MAS and version
        assertEquals(beMas, getOccurrences(updatedBeSshXml, REGEX_MAS).iterator().next());
        assertEquals(nlMas, getOccurrences(updatedNlSshXml, REGEX_MAS).iterator().next());
        assertEquals(String.valueOf(originalVersion + 1), getOccurrences(updatedBeSshXml, REGEX_VERSION).iterator().next());
        assertEquals(String.valueOf(originalVersion + 1), getOccurrences(updatedNlSshXml, REGEX_VERSION).iterator().next());
    }

    private CgmesMetadataModel getSubsetModel(Network network, CgmesSubset subset) {
        return network.getExtension(CgmesMetadataModels.class).getModelForSubset(subset).orElseThrow();
    }

    private Set<String> getOccurrences(String xml, Pattern pattern) {
        Set<String> matches = new HashSet<>();
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        return matches;
    }

}
