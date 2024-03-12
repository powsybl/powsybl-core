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
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CommonGridModelExportTest extends AbstractSerDeTest {

    @Test
    void testAssembled() throws IOException {
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

        // Obtain an assembled set of IGMs
        // Each IGM is a Subnetwork in the CGM Network
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource());
        assertEquals(2, network.getSubnetworks().size());
        debugCGM(network);

        Set<String> expectedTPs = network.getSubnetworks().stream().map(
                n -> n.getExtension(CgmesMetadataModels.class)
                        .getModelForPart(CgmesSubset.TOPOLOGY)
                        .map(CgmesMetadataModels.Model::getId)
                        .orElseThrow())
                .collect(Collectors.toSet());
        assertEquals(
                Set.of("urn:uuid:5d32d257-1646-4906-a1f6-4d7ce3f91569", "urn:uuid:f2f43818-09c8-4252-9611-7af80c398d20"),
                expectedTPs);

        // Export the SV for the CGM
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, "SV");
        String basename = network.getNameOrId();
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        Set<String> svDependentOns = dependentOns(read(basename, "SV"));
        debugSvDependentOns(svDependentOns);

        // All IGM TPs must be present in the SV dependentOns
        assertTrue(svDependentOns.containsAll(expectedTPs));
    }

    private static final Pattern REGEX_DEPENDENT_ON = Pattern.compile("Model.DependentOn rdf:resource=\"(.*?)\"");
    
    Set<String> dependentOns(String xml) {
        Set<String> matches = new HashSet<>();
        Matcher matcher = REGEX_DEPENDENT_ON.matcher(xml);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        return matches;
    }

    private String read(String basename, String profile) throws IOException {
        String instanceFile = String.format("%s_%s.xml", basename, profile);
        return Files.readString(tmpDir.resolve(instanceFile));
    }

    private static void debugCGM(Network network) {
        System.out.println("IGM subnetworks:");
        network.getSubnetworks().forEach(n -> {
            System.out.printf("  subnetwork : %s%n", n.getSubstations().iterator().next().getCountry().orElseThrow());
            System.out.printf("         SSH : %s%n",
                    n.getExtension(CgmesMetadataModels.class).getModelForPart(CgmesSubset.STEADY_STATE_HYPOTHESIS).map(CgmesMetadataModels.Model::getId).orElse("uknown"));
            System.out.printf("          TP : %s%n",
                    n.getExtension(CgmesMetadataModels.class).getModelForPart(CgmesSubset.TOPOLOGY).map(CgmesMetadataModels.Model::getId).orElse("uknown"));
        });
    }

    private void debugSvDependentOns(Set<String> svDependentOns) {
        System.out.println();
        System.out.println("SV dependentOns:");
        System.out.println(Arrays.toString(svDependentOns.toArray()));
    }

}
