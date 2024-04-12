/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.extensions.CgmesMetadataModelsAdder;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Summary from CGM Building Process Implementation Guide:
 * A CGM is created by assembling a set of IGMs for the same scenarioTime.
 * A Merging Agent is responsible for building the CGM.
 * The IGMs are first validated for plausibility by solving a power flow.
 * The CGM is then assembled from IGMs and a power flow is solved,
 * potentially adjusting some of the IGMs power flow hypothesis.
 * The Merging Agent provides an updated SSH for each IGM,
 * containing a md:Model.Supersedes reference to the IGM’s original SSH CIMXML file
 * and a single SV for the whole CGM, containing the results of power flow calculation.
 * The SV file must contain a md:Model.DependentOn reference to each IGM’s updated SSH.
 * The power flow of a CGM is calculated without topology processing. No new TP CIMXML
 * files are created. IGM TP files are used as an input. No TP fie is created as the result of CGM building.
 * This means that the CGM SV file must contain a md:Model.DependentOn reference to each IGM’s original TP.
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CommonGridModelExportTest extends AbstractSerDeTest {

    private static final Pattern REGEX_SCENARIO_TIME = Pattern.compile("Model.scenarioTime>(.*?)<");
    private static final Pattern REGEX_DESCRIPTION = Pattern.compile("Model.description>(.*?)<");
    private static final Pattern REGEX_VERSION = Pattern.compile("Model.version>(.*?)<");
    private static final Pattern REGEX_DEPENDENT_ON = Pattern.compile("Model.DependentOn rdf:resource=\"(.*?)\"");
    private static final Pattern REGEX_SUPERSEDES = Pattern.compile("Model.Supersedes rdf:resource=\"(.*?)\"");
    private static final Pattern REGEX_PROFILE = Pattern.compile("Model.profile>(.*?)<");
    private static final Pattern REGEX_MAS = Pattern.compile("Model.modelingAuthoritySet>(.*?)<");

    @Test
    void testCgmExportNoModelsNoProperties() throws IOException {
        // Create a simple network with two subnetworks
        Network network = bareNetwork2Subnetworks();

        // Perform a CGM export and read the exported files
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.CGM_EXPORT, true);
        String basename = "test_bare";
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        String updatedBeSshXml = Files.readString(tmpDir.resolve(basename + "_BE_SSH.xml"));
        String updatedNlSshXml = Files.readString(tmpDir.resolve(basename + "_NL_SSH.xml"));
        String updatedCgmSvXml = Files.readString(tmpDir.resolve(basename + "_SV.xml"));

        // Scenario time should be the same for all models
        assertEquals("2021-02-03T04:30:00Z", getOccurrences(updatedBeSshXml, REGEX_SCENARIO_TIME).iterator().next());
        assertEquals("2021-02-03T04:30:00Z", getOccurrences(updatedNlSshXml, REGEX_SCENARIO_TIME).iterator().next());
        assertEquals("2021-02-03T04:30:00Z", getOccurrences(updatedCgmSvXml, REGEX_SCENARIO_TIME).iterator().next());

        // Description should be the default one
        assertEquals("SSH Model", getOccurrences(updatedBeSshXml, REGEX_DESCRIPTION).iterator().next());
        assertEquals("SSH Model", getOccurrences(updatedNlSshXml, REGEX_DESCRIPTION).iterator().next());
        assertEquals("SV Model", getOccurrences(updatedCgmSvXml, REGEX_DESCRIPTION).iterator().next());

        // There is no version number for original models, so if exported as IGM they would have version equals to 1
        // Version number for updated models is increased by 1, so it equals to 2 in the end
        assertEquals("2", getOccurrences(updatedBeSshXml, REGEX_VERSION).iterator().next());
        assertEquals("2", getOccurrences(updatedNlSshXml, REGEX_VERSION).iterator().next());
        assertEquals("2", getOccurrences(updatedCgmSvXml, REGEX_VERSION).iterator().next());

        // The updated CGM SV should depend on the updated IGMs SSH and on the original IGMs TP
        // Here the version number part of the id 1 for original models and 2 for updated ones
        String updatedBeSshId = "urn:uuid:Network_BE_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_2_1D__FM";
        String updatedNlSshId = "urn:uuid:Network_NL_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_2_1D__FM";
        String originalBeTpId = "urn:uuid:Network_BE_N_TOPOLOGY_2021-02-03T04:30:00Z_1_1D__FM";
        String originalNlTpId = "urn:uuid:Network_NL_N_TOPOLOGY_2021-02-03T04:30:00Z_1_1D__FM";
        Set<String> expectedDependencies = Set.of(updatedBeSshId, updatedNlSshId, originalBeTpId, originalNlTpId);
        assertEquals(expectedDependencies, getOccurrences(updatedCgmSvXml, REGEX_DEPENDENT_ON));

        // Each updated IGM SSH should supersede the original one
        String originalBeSshId = "urn:uuid:Network_BE_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_1_1D__FM";
        String originalNlSshId = "urn:uuid:Network_NL_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_1_1D__FM";
        assertEquals(originalBeSshId, getOccurrences(updatedBeSshXml, REGEX_SUPERSEDES).iterator().next());
        assertEquals(originalNlSshId, getOccurrences(updatedNlSshXml, REGEX_SUPERSEDES).iterator().next());

        // Profiles should be consistent with the instance files
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getOccurrences(updatedBeSshXml, REGEX_PROFILE).iterator().next());
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getOccurrences(updatedNlSshXml, REGEX_PROFILE).iterator().next());
        assertEquals("http://entsoe.eu/CIM/StateVariables/4/1", getOccurrences(updatedCgmSvXml, REGEX_PROFILE).iterator().next());

        // All MAS should be equal to the default one since none has been provided
        assertEquals("powsybl.org", getOccurrences(updatedBeSshXml, REGEX_MAS).iterator().next());
        assertEquals("powsybl.org", getOccurrences(updatedNlSshXml, REGEX_MAS).iterator().next());
        assertEquals("powsybl.org", getOccurrences(updatedCgmSvXml, REGEX_MAS).iterator().next());
    }

    @Test
    void testCgmExportWithModelsForSubnetworks() throws IOException {
        // Create a simple network with two subnetworks
        Network network = bareNetwork2Subnetworks();
        addModelsForSubnetworks(network);

        // Perform a CGM export and read the exported files
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.CGM_EXPORT, true);
        String basename = "test_bare+submodels";
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        String updatedBeSshXml = Files.readString(tmpDir.resolve(basename + "_BE_SSH.xml"));
        String updatedNlSshXml = Files.readString(tmpDir.resolve(basename + "_NL_SSH.xml"));
        String updatedCgmSvXml = Files.readString(tmpDir.resolve(basename + "_SV.xml"));

        // Scenario time should be the same for all models
        assertEquals("2021-02-03T04:30:00Z", getOccurrences(updatedBeSshXml, REGEX_SCENARIO_TIME).iterator().next());
        assertEquals("2021-02-03T04:30:00Z", getOccurrences(updatedNlSshXml, REGEX_SCENARIO_TIME).iterator().next());
        assertEquals("2021-02-03T04:30:00Z", getOccurrences(updatedCgmSvXml, REGEX_SCENARIO_TIME).iterator().next());

        // IGM descriptions should be the ones provided in subnetwork models, CGM description should be the default one
        assertEquals("BE network description", getOccurrences(updatedBeSshXml, REGEX_DESCRIPTION).iterator().next());
        assertEquals("NL network description", getOccurrences(updatedNlSshXml, REGEX_DESCRIPTION).iterator().next());
        assertEquals("SV Model", getOccurrences(updatedCgmSvXml, REGEX_DESCRIPTION).iterator().next());

        // Version number should be increased from original models and be the same for all instance files
        assertEquals("2", getOccurrences(updatedBeSshXml, REGEX_VERSION).iterator().next());
        assertEquals("2", getOccurrences(updatedNlSshXml, REGEX_VERSION).iterator().next());
        assertEquals("2", getOccurrences(updatedCgmSvXml, REGEX_VERSION).iterator().next());

        // The updated CGM SV should depend on the updated IGMs SSH and on the original IGMs TP
        String updatedBeSshId = "urn:uuid:Network_BE_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_2_1D__FM";
        String updatedNlSshId = "urn:uuid:Network_NL_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_2_1D__FM";
        String originalBeTpId = "urn:uuid:Network_BE_N_TOPOLOGY_2021-02-03T04:30:00Z_1_1D__FM";
        String originalNlTpId = "urn:uuid:Network_NL_N_TOPOLOGY_2021-02-03T04:30:00Z_1_1D__FM";
        Set<String> expectedDependencies = Set.of(updatedBeSshId, updatedNlSshId, originalBeTpId, originalNlTpId);
        assertEquals(expectedDependencies, getOccurrences(updatedCgmSvXml, REGEX_DEPENDENT_ON));

        // Each updated IGM SSH should supersede the original one
        String originalBeSshId = "urn:uuid:Network_BE_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_1_1D__FM";
        String originalNlSshId = "urn:uuid:Network_NL_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_1_1D__FM";
        assertEquals(originalBeSshId, getOccurrences(updatedBeSshXml, REGEX_SUPERSEDES).iterator().next());
        assertEquals(originalNlSshId, getOccurrences(updatedNlSshXml, REGEX_SUPERSEDES).iterator().next());

        // Profiles should be consistent with the instance files
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getOccurrences(updatedBeSshXml, REGEX_PROFILE).iterator().next());
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getOccurrences(updatedNlSshXml, REGEX_PROFILE).iterator().next());
        assertEquals("http://entsoe.eu/CIM/StateVariables/4/1", getOccurrences(updatedCgmSvXml, REGEX_PROFILE).iterator().next());

        // IGM MAS should be the ones provided in subnetwork models, CGM MAS should be the default one
        assertEquals("http://elia.be/CGMES/2.4.15", getOccurrences(updatedBeSshXml, REGEX_MAS).iterator().next());
        assertEquals("http://tennet.nl/CGMES/2.4.15", getOccurrences(updatedNlSshXml, REGEX_MAS).iterator().next());
        assertEquals("powsybl.org", getOccurrences(updatedCgmSvXml, REGEX_MAS).iterator().next());
    }

    @Test
    void testCgmExportWithModelsForAllNetworks() throws IOException {
        // Create a simple network with two subnetworks
        Network network = bareNetwork2Subnetworks();
        addModelsForSubnetworks(network);
        addModelForNetwork(network);

        // Perform a CGM export and read the exported files
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.CGM_EXPORT, true);
        String basename = "test_bare+models";
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        String updatedBeSshXml = Files.readString(tmpDir.resolve(basename + "_BE_SSH.xml"));
        String updatedNlSshXml = Files.readString(tmpDir.resolve(basename + "_NL_SSH.xml"));
        String updatedCgmSvXml = Files.readString(tmpDir.resolve(basename + "_SV.xml"));

        // The main network has a different scenario time than the subnetworks
        // All updated models should get that scenario time
        assertEquals("2022-03-04T05:30:00Z", getOccurrences(updatedBeSshXml, REGEX_SCENARIO_TIME).iterator().next());
        assertEquals("2022-03-04T05:30:00Z", getOccurrences(updatedNlSshXml, REGEX_SCENARIO_TIME).iterator().next());
        assertEquals("2022-03-04T05:30:00Z", getOccurrences(updatedCgmSvXml, REGEX_SCENARIO_TIME).iterator().next());

        // IGM descriptions should be the ones provided in subnetwork models, CGM description should be the one provided in main network model
        assertEquals("BE network description", getOccurrences(updatedBeSshXml, REGEX_DESCRIPTION).iterator().next());
        assertEquals("NL network description", getOccurrences(updatedNlSshXml, REGEX_DESCRIPTION).iterator().next());
        assertEquals("Merged network description", getOccurrences(updatedCgmSvXml, REGEX_DESCRIPTION).iterator().next());

        // The main network has a different version number than the subnetworks
        // All updated models should get that version number
        assertEquals("3", getOccurrences(updatedBeSshXml, REGEX_VERSION).iterator().next());
        assertEquals("3", getOccurrences(updatedNlSshXml, REGEX_VERSION).iterator().next());
        assertEquals("3", getOccurrences(updatedCgmSvXml, REGEX_VERSION).iterator().next());

        // The updated CGM SV should depend on the updated IGMs SSH and on the original IGMs TP
        // The model of the main network brings an additional dependency
        String updatedBeSshId = "urn:uuid:Network_BE_N_STEADY_STATE_HYPOTHESIS_2022-03-04T05:30:00Z_3_1D__FM";
        String updatedNlSshId = "urn:uuid:Network_NL_N_STEADY_STATE_HYPOTHESIS_2022-03-04T05:30:00Z_3_1D__FM";
        String originalBeTpId = "urn:uuid:Network_BE_N_TOPOLOGY_2022-03-04T05:30:00Z_1_1D__FM";
        String originalNlTpId = "urn:uuid:Network_NL_N_TOPOLOGY_2022-03-04T05:30:00Z_1_1D__FM";
        String additionalDependency = "Additional dependency";
        Set<String> expectedDependencies = Set.of(updatedBeSshId, updatedNlSshId, originalBeTpId, originalNlTpId, additionalDependency);
        assertEquals(expectedDependencies, getOccurrences(updatedCgmSvXml, REGEX_DEPENDENT_ON));

        // Each updated IGM SSH should supersede the original one
        String originalBeSshId = "urn:uuid:Network_BE_N_STEADY_STATE_HYPOTHESIS_2022-03-04T05:30:00Z_1_1D__FM";
        String originalNlSshId = "urn:uuid:Network_NL_N_STEADY_STATE_HYPOTHESIS_2022-03-04T05:30:00Z_1_1D__FM";
        assertEquals(originalBeSshId, getOccurrences(updatedBeSshXml, REGEX_SUPERSEDES).iterator().next());
        assertEquals(originalNlSshId, getOccurrences(updatedNlSshXml, REGEX_SUPERSEDES).iterator().next());

        // Profiles should be consistent with the instance files
        // The model of the main network brings an additional profile
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getOccurrences(updatedBeSshXml, REGEX_PROFILE).iterator().next());
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getOccurrences(updatedNlSshXml, REGEX_PROFILE).iterator().next());
        Set<String> expectedProfiles = Set.of("Additional profile", "http://entsoe.eu/CIM/StateVariables/4/1");
        assertEquals(expectedProfiles, getOccurrences(updatedCgmSvXml, REGEX_PROFILE));

        // IGM MAS should be the ones provided in subnetwork models, CGM MAS should be the one provided in main network model
        assertEquals("http://elia.be/CGMES/2.4.15", getOccurrences(updatedBeSshXml, REGEX_MAS).iterator().next());
        assertEquals("http://tennet.nl/CGMES/2.4.15", getOccurrences(updatedNlSshXml, REGEX_MAS).iterator().next());
        assertEquals("Modeling Authority", getOccurrences(updatedCgmSvXml, REGEX_MAS).iterator().next());
    }

    @Test
    void testCgmExportWithProperties() throws IOException {
        // Create a simple network with two subnetworks
        Network network = bareNetwork2Subnetworks();

        // Perform a CGM export and read the exported files
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.CGM_EXPORT, true);
        exportParams.put(CgmesExport.MODELING_AUTHORITY_SET, "Regional Coordination Center");
        exportParams.put(CgmesExport.MODEL_DESCRIPTION, "Common Grid Model export");
        exportParams.put(CgmesExport.MODEL_VERSION, "4");
        String basename = "test_bare+properties";
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        String updatedBeSshXml = Files.readString(tmpDir.resolve(basename + "_BE_SSH.xml"));
        String updatedNlSshXml = Files.readString(tmpDir.resolve(basename + "_NL_SSH.xml"));
        String updatedCgmSvXml = Files.readString(tmpDir.resolve(basename + "_SV.xml"));

        // Scenario time should be the same for all models
        assertEquals("2021-02-03T04:30:00Z", getOccurrences(updatedBeSshXml, REGEX_SCENARIO_TIME).iterator().next());
        assertEquals("2021-02-03T04:30:00Z", getOccurrences(updatedNlSshXml, REGEX_SCENARIO_TIME).iterator().next());
        assertEquals("2021-02-03T04:30:00Z", getOccurrences(updatedCgmSvXml, REGEX_SCENARIO_TIME).iterator().next());

        // Description should be the one provided as parameter and be the same for all instance files
        assertEquals("Common Grid Model export", getOccurrences(updatedBeSshXml, REGEX_DESCRIPTION).iterator().next());
        assertEquals("Common Grid Model export", getOccurrences(updatedNlSshXml, REGEX_DESCRIPTION).iterator().next());
        assertEquals("Common Grid Model export", getOccurrences(updatedCgmSvXml, REGEX_DESCRIPTION).iterator().next());

        // Version number should be the one provided as parameter and be the same for all instance files
        assertEquals("4", getOccurrences(updatedBeSshXml, REGEX_VERSION).iterator().next());
        assertEquals("4", getOccurrences(updatedNlSshXml, REGEX_VERSION).iterator().next());
        assertEquals("4", getOccurrences(updatedCgmSvXml, REGEX_VERSION).iterator().next());

        // The updated CGM SV should depend on the updated IGMs SSH and on the original IGMs TP
        String updatedBeSshId = "urn:uuid:Network_BE_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_4_1D__FM";
        String updatedNlSshId = "urn:uuid:Network_NL_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_4_1D__FM";
        String originalBeTpId = "urn:uuid:Network_BE_N_TOPOLOGY_2021-02-03T04:30:00Z_1_1D__FM";
        String originalNlTpId = "urn:uuid:Network_NL_N_TOPOLOGY_2021-02-03T04:30:00Z_1_1D__FM";
        Set<String> expectedDependencies = Set.of(updatedBeSshId, updatedNlSshId, originalBeTpId, originalNlTpId);
        assertEquals(expectedDependencies, getOccurrences(updatedCgmSvXml, REGEX_DEPENDENT_ON));

        // Each updated IGM SSH should supersede the original one
        String originalBeSshId = "urn:uuid:Network_BE_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_1_1D__FM";
        String originalNlSshId = "urn:uuid:Network_NL_N_STEADY_STATE_HYPOTHESIS_2021-02-03T04:30:00Z_1_1D__FM";
        assertEquals(originalBeSshId, getOccurrences(updatedBeSshXml, REGEX_SUPERSEDES).iterator().next());
        assertEquals(originalNlSshId, getOccurrences(updatedNlSshXml, REGEX_SUPERSEDES).iterator().next());

        // Profiles should be consistent with the instance files
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getOccurrences(updatedBeSshXml, REGEX_PROFILE).iterator().next());
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getOccurrences(updatedNlSshXml, REGEX_PROFILE).iterator().next());
        assertEquals("http://entsoe.eu/CIM/StateVariables/4/1", getOccurrences(updatedCgmSvXml, REGEX_PROFILE).iterator().next());

        // IGM MAS should be the default ones, CGM MAS should be the one provided as parameter
        assertEquals("powsybl.org", getOccurrences(updatedBeSshXml, REGEX_MAS).iterator().next());
        assertEquals("powsybl.org", getOccurrences(updatedNlSshXml, REGEX_MAS).iterator().next());
        assertEquals("Regional Coordination Center", getOccurrences(updatedCgmSvXml, REGEX_MAS).iterator().next());
    }

    @Test
    void testCgmExportWithModelsAndProperties() throws IOException {
        // Create a simple network with two subnetworks
        Network network = bareNetwork2Subnetworks();
        addModelsForSubnetworks(network);
        addModelForNetwork(network);

        // Perform a CGM export and read the exported files
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.CGM_EXPORT, true);
        exportParams.put(CgmesExport.MODELING_AUTHORITY_SET, "Regional Coordination Center");
        exportParams.put(CgmesExport.MODEL_DESCRIPTION, "Common Grid Model export");
        exportParams.put(CgmesExport.MODEL_VERSION, "4");
        String basename = "test_bare+models+properties";
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        String updatedBeSshXml = Files.readString(tmpDir.resolve(basename + "_BE_SSH.xml"));
        String updatedNlSshXml = Files.readString(tmpDir.resolve(basename + "_NL_SSH.xml"));
        String updatedCgmSvXml = Files.readString(tmpDir.resolve(basename + "_SV.xml"));

        // The main network has a different scenario time than the subnetworks
        // All updated models should get that scenario time
        assertEquals("2022-03-04T05:30:00Z", getOccurrences(updatedBeSshXml, REGEX_SCENARIO_TIME).iterator().next());
        assertEquals("2022-03-04T05:30:00Z", getOccurrences(updatedNlSshXml, REGEX_SCENARIO_TIME).iterator().next());
        assertEquals("2022-03-04T05:30:00Z", getOccurrences(updatedCgmSvXml, REGEX_SCENARIO_TIME).iterator().next());

        // Both the models and a property define the description. The property should prevail.
        assertEquals("Common Grid Model export", getOccurrences(updatedBeSshXml, REGEX_DESCRIPTION).iterator().next());
        assertEquals("Common Grid Model export", getOccurrences(updatedNlSshXml, REGEX_DESCRIPTION).iterator().next());
        assertEquals("Common Grid Model export", getOccurrences(updatedCgmSvXml, REGEX_DESCRIPTION).iterator().next());

        // Both the models and a property define the version number. The property should prevail.
        assertEquals("4", getOccurrences(updatedBeSshXml, REGEX_VERSION).iterator().next());
        assertEquals("4", getOccurrences(updatedNlSshXml, REGEX_VERSION).iterator().next());
        assertEquals("4", getOccurrences(updatedCgmSvXml, REGEX_VERSION).iterator().next());

        // The updated CGM SV should depend on the updated IGMs SSH and on the original IGMs TP
        // The model of the main network brings an additional dependency
        String updatedBeSshId = "urn:uuid:Network_BE_N_STEADY_STATE_HYPOTHESIS_2022-03-04T05:30:00Z_4_1D__FM";
        String updatedNlSshId = "urn:uuid:Network_NL_N_STEADY_STATE_HYPOTHESIS_2022-03-04T05:30:00Z_4_1D__FM";
        String originalBeTpId = "urn:uuid:Network_BE_N_TOPOLOGY_2022-03-04T05:30:00Z_1_1D__FM";
        String originalNlTpId = "urn:uuid:Network_NL_N_TOPOLOGY_2022-03-04T05:30:00Z_1_1D__FM";
        String additionalDependency = "Additional dependency";
        Set<String> expectedDependencies = Set.of(updatedBeSshId, updatedNlSshId, originalBeTpId, originalNlTpId, additionalDependency);
        assertEquals(expectedDependencies, getOccurrences(updatedCgmSvXml, REGEX_DEPENDENT_ON));

        // Each updated IGM SSH should supersede the original one
        String originalBeSshId = "urn:uuid:Network_BE_N_STEADY_STATE_HYPOTHESIS_2022-03-04T05:30:00Z_1_1D__FM";
        String originalNlSshId = "urn:uuid:Network_NL_N_STEADY_STATE_HYPOTHESIS_2022-03-04T05:30:00Z_1_1D__FM";
        assertEquals(originalBeSshId, getOccurrences(updatedBeSshXml, REGEX_SUPERSEDES).iterator().next());
        assertEquals(originalNlSshId, getOccurrences(updatedNlSshXml, REGEX_SUPERSEDES).iterator().next());

        // Profiles should be consistent with the instance files, CGM SV has an additional profile
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getOccurrences(updatedBeSshXml, REGEX_PROFILE).iterator().next());
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getOccurrences(updatedNlSshXml, REGEX_PROFILE).iterator().next());
        Set<String> expectedProfiles = Set.of("Additional profile", "http://entsoe.eu/CIM/StateVariables/4/1");
        assertEquals(expectedProfiles, getOccurrences(updatedCgmSvXml, REGEX_PROFILE));

        // Both the model and a property define the main network MAS. The property should prevail.
        assertEquals("http://elia.be/CGMES/2.4.15", getOccurrences(updatedBeSshXml, REGEX_MAS).iterator().next());
        assertEquals("http://tennet.nl/CGMES/2.4.15", getOccurrences(updatedNlSshXml, REGEX_MAS).iterator().next());
        assertEquals("Regional Coordination Center", getOccurrences(updatedCgmSvXml, REGEX_MAS).iterator().next());
    }

    private Network bareNetwork2Subnetworks() {
        Network network1 = Network
                .create("Network_BE", "test")
                .newSubstation().setId("Substation1").setCountry(Country.BE).add()
                .getNetwork();
        network1.setCaseDate(ZonedDateTime.parse("2021-02-03T04:30:00.000+00:00"));

        Network network2 = Network
                .create("Network_NL", "test")
                .newSubstation().setId("Substation2").setCountry(Country.NL).add()
                .getNetwork();
        network2.setCaseDate(ZonedDateTime.parse("2021-02-03T04:30:00.000+00:00"));

        return Network.merge(network1, network2);
    }

    private void addModelsForSubnetworks(Network network) {
        // Add a model to the 2 Subnetworks
        network.getSubnetwork("Network_BE")
                .newExtension(CgmesMetadataModelsAdder.class)
                .newModel()
                .setSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS)
                .setDescription("BE network description")
                .setVersion(1)
                .setModelingAuthoritySet("http://elia.be/CGMES/2.4.15")
                .addDependentOn("BE EQ model ID")
                .addProfile("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1")
                .add()
                .add();
        network.getSubnetwork("Network_NL")
                .newExtension(CgmesMetadataModelsAdder.class)
                .newModel()
                .setSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS)
                .setDescription("NL network description")
                .setVersion(1)
                .setModelingAuthoritySet("http://tennet.nl/CGMES/2.4.15")
                .addDependentOn("NL EQ model ID")
                .addProfile("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1")
                .add()
                .add();
    }

    private void addModelForNetwork(Network network) {
        // Add a model to the merged network
        network.setCaseDate(ZonedDateTime.parse("2022-03-04T05:30:00.000+00:00"))
                .newExtension(CgmesMetadataModelsAdder.class)
                .newModel()
                .setSubset(CgmesSubset.STATE_VARIABLES)
                .setDescription("Merged network description")
                .setVersion(3)
                .setModelingAuthoritySet("Modeling Authority")
                .addDependentOn("Additional dependency")
                .addProfile("Additional profile")
                .add()
                .add();
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
