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
import com.powsybl.cgmes.extensions.CgmesMetadataModelsAdder;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
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
    void testIgmExportNoModelsNoPropertiesVersion() throws IOException {
        Network network = bareNetwork2Subnetworks();
        // Perform a simple IGM export and read the exported files
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.CGM_EXPORT, false);
        String basename = "test_bare_igm_be";
        network.getSubnetwork("Network_BE").write("CGMES", exportParams, tmpDir.resolve(basename));
        String exportedBeSshXml = Files.readString(tmpDir.resolve(basename + "_SSH.xml"));
        String exportedBeSvXml = Files.readString(tmpDir.resolve(basename + "_SV.xml"));

        // There is no version number for original models, so if exported as IGM they would have version equals to 1
        assertEquals("1", getFirstOccurrence(exportedBeSshXml, REGEX_VERSION));
        assertEquals("1", getFirstOccurrence(exportedBeSvXml, REGEX_VERSION));
    }

    @Test
    void testCgmExportWithModelsVersion() throws IOException {
        Network network = bareNetwork2Subnetworks();
        addModelsForSubnetworks(network, 0);

        // Perform a CGM export and read the exported files
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.CGM_EXPORT, true);
        String basename = "test_bare_models_0";
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        String updatedBeSshXml = Files.readString(tmpDir.resolve(basename + "_BE_SSH.xml"));
        String updatedNlSshXml = Files.readString(tmpDir.resolve(basename + "_NL_SSH.xml"));
        String updatedCgmSvXml = Files.readString(tmpDir.resolve(basename + "_SV.xml"));

        // Version number should be increased from original models and be the same for all instance files
        assertEquals("1", getFirstOccurrence(updatedBeSshXml, REGEX_VERSION));
        assertEquals("1", getFirstOccurrence(updatedNlSshXml, REGEX_VERSION));
        assertEquals("1", getFirstOccurrence(updatedCgmSvXml, REGEX_VERSION));

    }

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
        assertEquals("2021-02-03T04:30:00Z", getFirstOccurrence(updatedBeSshXml, REGEX_SCENARIO_TIME));
        assertEquals("2021-02-03T04:30:00Z", getFirstOccurrence(updatedNlSshXml, REGEX_SCENARIO_TIME));
        assertEquals("2021-02-03T04:30:00Z", getFirstOccurrence(updatedCgmSvXml, REGEX_SCENARIO_TIME));

        // Description should be the default one
        assertEquals("SSH Model", getFirstOccurrence(updatedBeSshXml, REGEX_DESCRIPTION));
        assertEquals("SSH Model", getFirstOccurrence(updatedNlSshXml, REGEX_DESCRIPTION));
        assertEquals("SV Model", getFirstOccurrence(updatedCgmSvXml, REGEX_DESCRIPTION));

        // There is no version number for original models, so if exported as IGM they would have version equals to 1
        // Version number for updated models is increased by 1, so it equals to 2 in the end
        assertEquals("2", getFirstOccurrence(updatedBeSshXml, REGEX_VERSION));
        assertEquals("2", getFirstOccurrence(updatedNlSshXml, REGEX_VERSION));
        assertEquals("2", getFirstOccurrence(updatedCgmSvXml, REGEX_VERSION));

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
        assertEquals(originalBeSshId, getFirstOccurrence(updatedBeSshXml, REGEX_SUPERSEDES));
        assertEquals(originalNlSshId, getFirstOccurrence(updatedNlSshXml, REGEX_SUPERSEDES));

        // Profiles should be consistent with the instance files
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getFirstOccurrence(updatedBeSshXml, REGEX_PROFILE));
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getFirstOccurrence(updatedNlSshXml, REGEX_PROFILE));
        assertEquals("http://entsoe.eu/CIM/StateVariables/4/1", getFirstOccurrence(updatedCgmSvXml, REGEX_PROFILE));

        // All MAS should be equal to the default one since none has been provided
        assertEquals("powsybl.org", getFirstOccurrence(updatedBeSshXml, REGEX_MAS));
        assertEquals("powsybl.org", getFirstOccurrence(updatedNlSshXml, REGEX_MAS));
        assertEquals("powsybl.org", getFirstOccurrence(updatedCgmSvXml, REGEX_MAS));
    }

    @Test
    void testCgmExportWithModelsForSubnetworks() throws IOException {
        // Create a simple network with two subnetworks
        Network network = bareNetwork2Subnetworks();
        addModelsForSubnetworks(network, 1);

        // Perform a CGM export and read the exported files
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.CGM_EXPORT, true);
        String basename = "test_bare+submodels";
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        String updatedBeSshXml = Files.readString(tmpDir.resolve(basename + "_BE_SSH.xml"));
        String updatedNlSshXml = Files.readString(tmpDir.resolve(basename + "_NL_SSH.xml"));
        String updatedCgmSvXml = Files.readString(tmpDir.resolve(basename + "_SV.xml"));

        // Scenario time should be the same for all models
        assertEquals("2021-02-03T04:30:00Z", getFirstOccurrence(updatedBeSshXml, REGEX_SCENARIO_TIME));
        assertEquals("2021-02-03T04:30:00Z", getFirstOccurrence(updatedNlSshXml, REGEX_SCENARIO_TIME));
        assertEquals("2021-02-03T04:30:00Z", getFirstOccurrence(updatedCgmSvXml, REGEX_SCENARIO_TIME));

        // IGM descriptions should be the ones provided in subnetwork models, CGM description should be the default one
        assertEquals("BE network description", getFirstOccurrence(updatedBeSshXml, REGEX_DESCRIPTION));
        assertEquals("NL network description", getFirstOccurrence(updatedNlSshXml, REGEX_DESCRIPTION));
        assertEquals("SV Model", getFirstOccurrence(updatedCgmSvXml, REGEX_DESCRIPTION));

        // Version number should be increased from original models and be the same for all instance files
        assertEquals("2", getFirstOccurrence(updatedBeSshXml, REGEX_VERSION));
        assertEquals("2", getFirstOccurrence(updatedNlSshXml, REGEX_VERSION));
        assertEquals("2", getFirstOccurrence(updatedCgmSvXml, REGEX_VERSION));

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
        assertEquals(originalBeSshId, getFirstOccurrence(updatedBeSshXml, REGEX_SUPERSEDES));
        assertEquals(originalNlSshId, getFirstOccurrence(updatedNlSshXml, REGEX_SUPERSEDES));

        // Profiles should be consistent with the instance files
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getFirstOccurrence(updatedBeSshXml, REGEX_PROFILE));
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getFirstOccurrence(updatedNlSshXml, REGEX_PROFILE));
        assertEquals("http://entsoe.eu/CIM/StateVariables/4/1", getFirstOccurrence(updatedCgmSvXml, REGEX_PROFILE));

        // IGM MAS should be the ones provided in subnetwork models, CGM MAS should be the default one
        assertEquals("http://elia.be/CGMES/2.4.15", getFirstOccurrence(updatedBeSshXml, REGEX_MAS));
        assertEquals("http://tennet.nl/CGMES/2.4.15", getFirstOccurrence(updatedNlSshXml, REGEX_MAS));
        assertEquals("powsybl.org", getFirstOccurrence(updatedCgmSvXml, REGEX_MAS));
    }

    @Test
    void testCgmExportWithModelsForAllNetworks() throws IOException {
        // Create a simple network with two subnetworks
        Network network = bareNetwork2Subnetworks();
        addModelsForSubnetworks(network, 1);
        addModelForNetwork(network, 3);

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
        assertEquals("2022-03-04T05:30:00Z", getFirstOccurrence(updatedBeSshXml, REGEX_SCENARIO_TIME));
        assertEquals("2022-03-04T05:30:00Z", getFirstOccurrence(updatedNlSshXml, REGEX_SCENARIO_TIME));
        assertEquals("2022-03-04T05:30:00Z", getFirstOccurrence(updatedCgmSvXml, REGEX_SCENARIO_TIME));

        // IGM descriptions should be the ones provided in subnetwork models, CGM description should be the one provided in main network model
        assertEquals("BE network description", getFirstOccurrence(updatedBeSshXml, REGEX_DESCRIPTION));
        assertEquals("NL network description", getFirstOccurrence(updatedNlSshXml, REGEX_DESCRIPTION));
        assertEquals("Merged network description", getFirstOccurrence(updatedCgmSvXml, REGEX_DESCRIPTION));

        // The main network has a different version number (3) than the subnetworks (1)
        // Updated models should use next version taking into account the max version number of inputs (next version is 4)
        assertEquals("4", getFirstOccurrence(updatedBeSshXml, REGEX_VERSION));
        assertEquals("4", getFirstOccurrence(updatedNlSshXml, REGEX_VERSION));
        assertEquals("4", getFirstOccurrence(updatedCgmSvXml, REGEX_VERSION));

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
        assertEquals(originalBeSshId, getFirstOccurrence(updatedBeSshXml, REGEX_SUPERSEDES));
        assertEquals(originalNlSshId, getFirstOccurrence(updatedNlSshXml, REGEX_SUPERSEDES));

        // Profiles should be consistent with the instance files
        // The model of the main network brings an additional profile
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getFirstOccurrence(updatedBeSshXml, REGEX_PROFILE));
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getFirstOccurrence(updatedNlSshXml, REGEX_PROFILE));
        Set<String> expectedProfiles = Set.of("Additional profile", "http://entsoe.eu/CIM/StateVariables/4/1");
        assertEquals(expectedProfiles, getOccurrences(updatedCgmSvXml, REGEX_PROFILE));

        // IGM MAS should be the ones provided in subnetwork models, CGM MAS should be the one provided in main network model
        assertEquals("http://elia.be/CGMES/2.4.15", getFirstOccurrence(updatedBeSshXml, REGEX_MAS));
        assertEquals("http://tennet.nl/CGMES/2.4.15", getFirstOccurrence(updatedNlSshXml, REGEX_MAS));
        assertEquals("Modeling Authority", getFirstOccurrence(updatedCgmSvXml, REGEX_MAS));
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
        assertEquals("2021-02-03T04:30:00Z", getFirstOccurrence(updatedBeSshXml, REGEX_SCENARIO_TIME));
        assertEquals("2021-02-03T04:30:00Z", getFirstOccurrence(updatedNlSshXml, REGEX_SCENARIO_TIME));
        assertEquals("2021-02-03T04:30:00Z", getFirstOccurrence(updatedCgmSvXml, REGEX_SCENARIO_TIME));

        // Description should be the one provided as parameter and be the same for all instance files
        assertEquals("Common Grid Model export", getFirstOccurrence(updatedBeSshXml, REGEX_DESCRIPTION));
        assertEquals("Common Grid Model export", getFirstOccurrence(updatedNlSshXml, REGEX_DESCRIPTION));
        assertEquals("Common Grid Model export", getFirstOccurrence(updatedCgmSvXml, REGEX_DESCRIPTION));

        // Version number should be the one provided as parameter and be the same for all instance files
        assertEquals("4", getFirstOccurrence(updatedBeSshXml, REGEX_VERSION));
        assertEquals("4", getFirstOccurrence(updatedNlSshXml, REGEX_VERSION));
        assertEquals("4", getFirstOccurrence(updatedCgmSvXml, REGEX_VERSION));

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
        assertEquals(originalBeSshId, getFirstOccurrence(updatedBeSshXml, REGEX_SUPERSEDES));
        assertEquals(originalNlSshId, getFirstOccurrence(updatedNlSshXml, REGEX_SUPERSEDES));

        // Profiles should be consistent with the instance files
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getFirstOccurrence(updatedBeSshXml, REGEX_PROFILE));
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getFirstOccurrence(updatedNlSshXml, REGEX_PROFILE));
        assertEquals("http://entsoe.eu/CIM/StateVariables/4/1", getFirstOccurrence(updatedCgmSvXml, REGEX_PROFILE));

        // IGM MAS should be the default ones, CGM MAS should be the one provided as parameter
        assertEquals("powsybl.org", getFirstOccurrence(updatedBeSshXml, REGEX_MAS));
        assertEquals("powsybl.org", getFirstOccurrence(updatedNlSshXml, REGEX_MAS));
        assertEquals("Regional Coordination Center", getFirstOccurrence(updatedCgmSvXml, REGEX_MAS));
    }

    @Test
    void testCgmExportWithModelsAndProperties() throws IOException {
        // Create a simple network with two subnetworks
        Network network = bareNetwork2Subnetworks();
        addModelsForSubnetworks(network, 1);
        addModelForNetwork(network, 2);

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
        assertEquals("2022-03-04T05:30:00Z", getFirstOccurrence(updatedBeSshXml, REGEX_SCENARIO_TIME));
        assertEquals("2022-03-04T05:30:00Z", getFirstOccurrence(updatedNlSshXml, REGEX_SCENARIO_TIME));
        assertEquals("2022-03-04T05:30:00Z", getFirstOccurrence(updatedCgmSvXml, REGEX_SCENARIO_TIME));

        // Both the models and a property define the description. The property should prevail.
        assertEquals("Common Grid Model export", getFirstOccurrence(updatedBeSshXml, REGEX_DESCRIPTION));
        assertEquals("Common Grid Model export", getFirstOccurrence(updatedNlSshXml, REGEX_DESCRIPTION));
        assertEquals("Common Grid Model export", getFirstOccurrence(updatedCgmSvXml, REGEX_DESCRIPTION));

        // Both the models and a property define the version number. The property should prevail.
        assertEquals("4", getFirstOccurrence(updatedBeSshXml, REGEX_VERSION));
        assertEquals("4", getFirstOccurrence(updatedNlSshXml, REGEX_VERSION));
        assertEquals("4", getFirstOccurrence(updatedCgmSvXml, REGEX_VERSION));

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
        assertEquals(originalBeSshId, getFirstOccurrence(updatedBeSshXml, REGEX_SUPERSEDES));
        assertEquals(originalNlSshId, getFirstOccurrence(updatedNlSshXml, REGEX_SUPERSEDES));

        // Profiles should be consistent with the instance files, CGM SV has an additional profile
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getFirstOccurrence(updatedBeSshXml, REGEX_PROFILE));
        assertEquals("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1", getFirstOccurrence(updatedNlSshXml, REGEX_PROFILE));
        Set<String> expectedProfiles = Set.of("Additional profile", "http://entsoe.eu/CIM/StateVariables/4/1");
        assertEquals(expectedProfiles, getOccurrences(updatedCgmSvXml, REGEX_PROFILE));

        // Both the model and a property define the main network MAS. The property should prevail.
        assertEquals("http://elia.be/CGMES/2.4.15", getFirstOccurrence(updatedBeSshXml, REGEX_MAS));
        assertEquals("http://tennet.nl/CGMES/2.4.15", getFirstOccurrence(updatedNlSshXml, REGEX_MAS));
        assertEquals("Regional Coordination Center", getFirstOccurrence(updatedCgmSvXml, REGEX_MAS));
    }

    @Test
    void testFaraoUseCase() {
        // We use MicroGrid base case assembled as the CGM model to export
        ReadOnlyDataSource ds = CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource();
        Network cgmNetwork = Network.read(ds);

        // Check the version of the individual files in the test case
        int currentVersion = readVersion(ds, "MicroGridTestConfiguration_BC_BE_SSH_V2.xml").orElseThrow();
        int sshNLVersion = readVersion(ds, "MicroGridTestConfiguration_BC_NL_SSH_V2.xml").orElseThrow();
        int svVersion = readVersion(ds, "MicroGridTestConfiguration_BC_Assembled_SV_V2.xml").orElseThrow();
        assertEquals(currentVersion, sshNLVersion);
        assertEquals(currentVersion, svVersion);
        Network[] ns = cgmNetwork.getSubnetworks().toArray(new Network[] {});
        assertEquals(currentVersion, ns[0].getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS).orElseThrow().getVersion());
        assertEquals(currentVersion, ns[1].getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS).orElseThrow().getVersion());

        // If we export this CGM we expected the output files to on current version + 1
        int expectedOutputVersion = currentVersion + 1;

        // Export with the same parameters of the FARAO use case
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, List.of("SV", "SSH"));
        exportParams.put(CgmesExport.EXPORT_BOUNDARY_POWER_FLOWS, true);
        exportParams.put(CgmesExport.NAMING_STRATEGY, "cgmes");
        exportParams.put(CgmesExport.CGM_EXPORT, true);
        exportParams.put(CgmesExport.UPDATE_DEPENDENCIES, true);
        exportParams.put(CgmesExport.MODELING_AUTHORITY_SET, "MAS1");

        MemDataSource memDataSource = new MemDataSource();
        cgmNetwork.write("CGMES", exportParams, memDataSource);
        for (Map.Entry<Country, String> entry : TSO_BY_COUNTRY.entrySet()) {
            Country country = entry.getKey();
            // For this unit test we do not need the TSO from the entry value
            String filenameFromCgmesExport = cgmNetwork.getNameOrId() + "_" + country.toString() + "_SSH.xml";

            // The CGM network does not have metadata models, only the subnetworks
            assertEquals((CgmesMetadataModels) null, cgmNetwork.getExtension(CgmesMetadataModels.class));

            // The metadata models in memory are NEVER updated after export (this is by design)
            // Look for the subnetwork of the current country
            Network nc = cgmNetwork.getSubnetworks().stream().filter(n -> country == n.getSubstations().iterator().next().getCountry().orElseThrow()).toList().get(0);
            int sshVersionInNetworkMetadataModels = nc.getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS).orElseThrow().getVersion();
            assertEquals(currentVersion, sshVersionInNetworkMetadataModels);

            // To know the exported version we should read what has been written in the output files
            int sshVersionInOutput = readVersion(memDataSource, filenameFromCgmesExport).orElseThrow();
            assertEquals(expectedOutputVersion, sshVersionInOutput);
        }
        String filenameFromCgmesExport = cgmNetwork.getNameOrId() + "_SV.xml";
        // We have to read it from inside the SV file ...
        int svVersionInOutput = readVersion(memDataSource, filenameFromCgmesExport).orElseThrow();
        assertEquals(expectedOutputVersion, svVersionInOutput);
    }

    private static final Map<Country, String> TSO_BY_COUNTRY = Map.of(
            Country.BE, "Elia",
            Country.NL, "Tennet");

    private static Optional<Integer> readVersion(ReadOnlyDataSource ds, String filename) {
        try {
            return readVersion(ds.newInputStream(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<Integer> readVersion(InputStream is) {
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            boolean insideModelVersion = false;
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("Model.version")) {
                    insideModelVersion = true;
                } else if (next == XMLStreamConstants.END_ELEMENT) {
                    insideModelVersion = false;
                } else if (next == XMLStreamConstants.CHARACTERS && insideModelVersion) {
                    String version = reader.getText();
                    reader.close();
                    return Optional.of(Integer.parseInt(version));
                }
            }
            reader.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
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

    private void addModelsForSubnetworks(Network network, int version) {
        // Add a model to the 2 Subnetworks
        network.getSubnetwork("Network_BE")
                .newExtension(CgmesMetadataModelsAdder.class)
                .newModel()
                .setSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS)
                .setDescription("BE network description")
                .setVersion(version)
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
                .setVersion(version)
                .setModelingAuthoritySet("http://tennet.nl/CGMES/2.4.15")
                .addDependentOn("NL EQ model ID")
                .addProfile("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1")
                .add()
                .add();
    }

    private void addModelForNetwork(Network network, int version) {
        // Add a model to the merged network
        network.setCaseDate(ZonedDateTime.parse("2022-03-04T05:30:00.000+00:00"))
                .newExtension(CgmesMetadataModelsAdder.class)
                .newModel()
                .setSubset(CgmesSubset.STATE_VARIABLES)
                .setDescription("Merged network description")
                .setVersion(version)
                .setModelingAuthoritySet("Modeling Authority")
                .addDependentOn("Additional dependency")
                .addProfile("Additional profile")
                .add()
                .add();
    }

    private String getFirstOccurrence(String xml, Pattern pattern) {
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new PowsyblException("Pattern not found " + pattern);
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
