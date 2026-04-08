/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export.issues;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class ConsiderValidMasterRIDWithLeadingUnderscoreTest extends AbstractSerDeTest {
    @Test
    void testSshWithCgmesNamingStrategyPreservesIdentifiers() throws IOException {
        // Create a very simple network with two loads:
        // One load will have a UUID as identifier,
        // The other one a UUID prefixed with an underscore
        Network network = NetworkFactory.findDefault().createNetwork("minimal-network", "test");
        Substation substation1 = network.newSubstation()
                .setId(newUuid())
                .setCountry(Country.AQ).add();
        VoltageLevel voltageLevel = substation1.newVoltageLevel()
                .setId(newUuid())
                .setNominalV(400).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        Bus bus = voltageLevel.getBusBreakerView().newBus()
                .setId(newUuid()).add();
        Load loadUuid = voltageLevel.newLoad()
                .setId(newUuid())
                .setBus(bus.getId()).setP0(0).setQ0(0).add();
        Load loadUnderscoreUuid = voltageLevel.newLoad()
                .setId("_" + newUuid())
                .setBus(bus.getId()).setP0(0).setQ0(0).add();

        // Export SSH using the "cgmes" naming strategy
        Set<String> exportedRdfAbouts = exportedSshRdfAboutsWithCgmesNamingStrategy(network);

        // And check that both load identifiers have been preserved in the output
        // All exported identifiers must be valid
        assertTrue(exportedRdfAbouts.stream().allMatch(CgmesExportUtil::isValidCimMasterRID));
        // Both IIDM load identifiers must be found in the exported data
        // The load with the leading underscore will be found directly as and rdf:about
        assertTrue(exportedRdfAbouts.contains(loadUnderscoreUuid.getId()));
        // The load without the leading underscore must have been exported as a rdf:about that has a leading underscore
        assertTrue(exportedRdfAbouts.contains("_" + loadUuid.getId()));
    }

    String newUuid() {
        return UUID.randomUUID().toString();
    }

    private Set<String> exportedSshRdfAboutsWithCgmesNamingStrategy(Network network) throws IOException {
        String basename = network.getNameOrId();

        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.NAMING_STRATEGY, "cgmes");
        exportParams.put(CgmesExport.PROFILES, "SSH");
        network.write("CGMES", exportParams, tmpDir.resolve(basename));

        String instanceFile = String.format("%s_%s.xml", basename, "SSH");
        String instanceFileContent = Files.readString(tmpDir.resolve(instanceFile));

        return extractRdfAbouts(instanceFileContent);
    }

    private static final Pattern REGEX_RDF_ABOUT = Pattern.compile(" rdf:about=\"[#]*(.*?)\"");

    private Set<String> extractRdfAbouts(String xml) {
        Set<String> ids = new HashSet<>();
        Matcher matcher = REGEX_RDF_ABOUT.matcher(xml);
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }
}
