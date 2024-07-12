/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.extensions.CgmesMetadataModelsAdder;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.Identifiables;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.powsybl.cgmes.conversion.Conversion.CGMES_PREFIX_ALIAS_PROPERTIES;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class LegacyCommonGridModelExportTest extends AbstractSerDeTest {

    @Test
    void testExportCgmSvDependenciesNotUpdated() throws IOException {
        // We check that prepared SV dependencies have not been modified even if we export as CGM (SSH + SV)
        // This is really not useful, since in this scenario we would want to export only SV
        // But we want to let the parameter "update-dependencies" be valid in both types of export: IGM and CGM

        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledSvWithMas().dataSource();
        Network network = Network.read(ds);

        List<String> sshIds = List.of("ssh-dep1", "ssh-dep2", "ssh-dep3");
        buildSvDependenciesManaginMetadataModels(network, sshIds);

        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.CGM_EXPORT, "True");
        exportParams.put(CgmesExport.UPDATE_DEPENDENCIES, "False");
        String exportBasename = "tmp-micro-bc-CGM";
        network.write("CGMES", exportParams, tmpDir.resolve(exportBasename));

        String sv = Files.readString(tmpDir.resolve(exportBasename + "_SV.xml"));

        Set<String> deps = findAll(REGEX_DEPENDENT_ON, sv);
        Set<String> prepared = network.getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.STATE_VARIABLES).orElseThrow().getDependentOn();
        assertEquals(prepared, deps);
    }

    @Test
    void testExportCgmSvDependenciesOnNetworkProperties() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledSvWithMas().dataSource();
        Network network = Network.read(ds);

        // This is the legacy way of preparing dependencies for SV externally,
        // It was used by projects in the FARAO community
        // The support for this way of preparing dependencies has been dropped
        network.setProperty(Identifiables.getUniqueId(CGMES_PREFIX_ALIAS_PROPERTIES + "SSH_ID", network::hasProperty), "ssh-updated-dep1");
        network.setProperty(Identifiables.getUniqueId(CGMES_PREFIX_ALIAS_PROPERTIES + "SSH_ID", network::hasProperty), "ssh-updated-dep2");
        network.setProperty(Identifiables.getUniqueId(CGMES_PREFIX_ALIAS_PROPERTIES + "SSH_ID", network::hasProperty), "ssh-updated-dep3");
        network.setProperty(Identifiables.getUniqueId(CGMES_PREFIX_ALIAS_PROPERTIES + "TP_ID", network::hasProperty), "tp-initial-dep1");
        network.setProperty(Identifiables.getUniqueId(CGMES_PREFIX_ALIAS_PROPERTIES + "TP_ID", network::hasProperty), "tp-initial-dep2");
        network.setProperty(Identifiables.getUniqueId(CGMES_PREFIX_ALIAS_PROPERTIES + "TP_ID", network::hasProperty), "tp-initial-dep3");

        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, "SV");
        String exportBasename = "tmp-micro-bc-from-CGM";
        network.write("CGMES", exportParams, tmpDir.resolve(exportBasename));

        Set<String> deps = findAll(REGEX_DEPENDENT_ON, Files.readString(tmpDir.resolve(exportBasename + "_SV.xml")));
        // We ensure that written dependencies do not match any of the prepared through properties
        Set<String> prepared = Set.of("ssh-updated-dep1", "ssh-updated-dep2", "ssh-updated-dep3", "tp-initial-dep1", "tp-initial-dep2", "tp-initial-dep3");
        assertFalse(deps.stream().anyMatch(prepared::contains));
    }

    @Test
    void testExportCgmSvDependenciesOnMetadataModelsExtension() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledSvWithMas().dataSource();
        Network network = Network.read(ds);
        List<String> initialSshIds = gatherInitialSshIds(network);

        // This test shows how to prepare the dependencies for SV in main network
        // by directly building the metadata model extension.
        // We pass only the updated SSH dependencies
        List<String> updatedSshIds = List.of("ssh-updated-dep1", "ssh-updated-dep2", "ssh-updated-dep3");
        buildSvDependenciesManaginMetadataModels(network, updatedSshIds);

        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, "SV");
        // Because we have built the SV metadata model explicitly,
        // We do not want the CGMES export module to update dependency information
        exportParams.put(CgmesExport.UPDATE_DEPENDENCIES, "False");
        String exportBasename = "tmp-micro-bc-from-CGM";
        network.write("CGMES", exportParams, tmpDir.resolve(exportBasename));

        String sv = Files.readString(tmpDir.resolve(exportBasename + "_SV.xml"));

        Set<String> deps = findAll(REGEX_DEPENDENT_ON, sv);
        assertTrue(deps.containsAll(updatedSshIds));
        initialSshIds.forEach(initialSshId -> assertFalse(deps.contains(initialSshId)));

        // Ensure that only the prepared dependencies have been put in the output
        Set<String> prepared = network.getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.STATE_VARIABLES).orElseThrow().getDependentOn();
        assertEquals(prepared, deps);

        assertEquals("MAS1", findFirst(MODELING_AUTHORITY, sv));
    }

    private void buildSvDependenciesManaginMetadataModels(Network network, List<String> updateSshIds) {
        CgmesMetadataModelsAdder networkModelsAdder = network.newExtension(CgmesMetadataModelsAdder.class);
        CgmesMetadataModelsAdder.ModelAdder svModelExport = networkModelsAdder.newModel();
        svModelExport.setSubset(CgmesSubset.STATE_VARIABLES);
        svModelExport.setId("a-unique-id");

        Network subnetwork = (Network) network.getSubnetworks().toArray()[0];
        CgmesMetadataModels modelsExtension = subnetwork.getExtension(CgmesMetadataModels.class);
        if (modelsExtension != null) {
            modelsExtension.getModelForSubset(CgmesSubset.STATE_VARIABLES).ifPresent(
                    svModel -> {
                        List<String> initialSvDependantOn = copyListDependencies(svModel);
                        removeInitialSshFromInitialDependencies(network, initialSvDependantOn);
                        initialSvDependantOn.forEach(svModelExport::addDependentOn);

                        svModelExport.setModelingAuthoritySet(svModel.getModelingAuthoritySet());
                        svModel.getProfiles().forEach(svModelExport::addProfile);
                        svModelExport.setDescription(svModel.getDescription());
                    }
            );
        }
        updateSshIds.forEach(svModelExport::addDependentOn);
        svModelExport.add();
        networkModelsAdder.add();
    }

    private static List<String> copyListDependencies(CgmesMetadataModel svModel) {
        if (svModel != null) {
            return new ArrayList<>(svModel.getDependentOn());
        }
        return new ArrayList<>();
    }

    private static List<String> gatherInitialSshIds(Network network) {
        return network.getSubnetworks()
                .stream()
                .map(subNetwork -> subNetwork.getExtension(CgmesMetadataModels.class))
                .filter(Objects::nonNull)
                .map(modelsExtension -> ((CgmesMetadataModels) modelsExtension).getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(CgmesMetadataModel::getId)
                .toList();
    }

    private static void removeInitialSshFromInitialDependencies(Network network, List<String> initialSvDependantOn) {
        gatherInitialSshIds(network).forEach(initialSvDependantOn::remove);
    }

    private static Set<String> findAll(Pattern pattern, String xml) {
        Set<String> found = new HashSet<>();
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found;
    }

    private static String findFirst(Pattern pattern, String xml) {
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static final Pattern REGEX_DEPENDENT_ON = Pattern.compile("<md:Model.DependentOn rdf:resource=\"(.*?)\"");
    private static final Pattern MODELING_AUTHORITY = Pattern.compile("<md:Model.modelingAuthoritySet>(.*?)</md:Model.modelingAuthoritySet>");
}
