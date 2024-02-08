/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export.issues;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.extensions.CgmesSshMetadataAdder;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class ModelIdTest extends AbstractSerDeTest {

    private static final Pattern REGEX_FULL_MODEL_ID = Pattern.compile("FullModel rdf:about=\"(.*?)\"");

    @Test
    void testModelIds() throws IOException {
        Network network = NetworkTest1Factory.create("minimal-network");

        // We will export the case for two different times: t0 and t1
        ZonedDateTime t0 = ZonedDateTime.of(2020, 1, 1, 0, 0, 30, 0, ZoneId.systemDefault());
        ZonedDateTime t1 = ZonedDateTime.of(2020, 1, 1, 1, 0, 30, 0, ZoneId.systemDefault());
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.NAMING_STRATEGY, "cgmes");

        // Check ids of the different export parts (EQ, SSH, SV, TP) are different
        network.setCaseDate(t0);
        Map<String, String> ids0 = exportedIds(network, exportParams);
        Set<String> uniqueIds = new HashSet<>(ids0.values());
        assertEquals(ids0.size(), uniqueIds.size());

        // Check we get different ids when scenarioTime changes
        network.setCaseDate(t1);
        Map<String, String> ids1 = exportedIds(network, exportParams);
        assertNotEquals(ids0.get("SSH"), ids1.get("SSH"));
        assertNotEquals(ids0.get("SV"), ids1.get("SV"));
        assertNotEquals(ids0.get("TP"), ids1.get("TP"));

        // Check that we get different ids when version changes
        // The way to force a different version number in the output
        // is to give a previous version number inside metadata extension
        // Export will increase the version number available in the metadata
        network.newExtension(CgmesSshMetadataAdder.class)
                .setId("not-relevant")
                .setDescription("not-relevant")
                .setSshVersion(42)
                .setModelingAuthoritySet("not-relevant")
                .addDependency("not-relevant")
                .add();
        Map<String, String> ids1v = exportedIds(network, exportParams);
        // We only have added previous version for SSH data, so only SSH id must have changed
        assertNotEquals(ids1.get("SSH"), ids1v.get("SSH"));
        assertEquals(ids1.get("SV"), ids1v.get("SV"));

        // Check that we get different ids when business process changes
        exportParams.put(CgmesExport.BUSINESS_PROCESS, "2D");
        Map<String, String> ids1BP = exportedIds(network, exportParams);
        assertNotEquals(ids1v.get("SSH"), ids1BP.get("SSH"));
        assertNotEquals(ids1v.get("SV"), ids1BP.get("SV"));

        // Check that we get different ids when version is set as a parameter
        exportParams.put(CgmesExport.MODEL_VERSION, "24");
        Map<String, String> ids1vparam = exportedIds(network, exportParams);
        assertNotEquals(ids1BP.get("SSH"), ids1vparam.get("SSH"));
        assertNotEquals(ids1BP.get("SV"), ids1vparam.get("SV"));
    }

    String modelId(String xml) {
        Matcher matcher = REGEX_FULL_MODEL_ID.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new PowsyblException("missing model id");
        }
    }

    private String read(String basename, String profile) throws IOException {
        String instanceFile = String.format("%s_%s.xml", basename, profile);
        return Files.readString(tmpDir.resolve(instanceFile));
    }

    private Map<String, String> exportedIds(Network network, Properties exportParams) throws IOException {
        String basename = network.getNameOrId();
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        return Map.of("EQ", modelId(read(basename, "EQ")),
                "SSH", modelId(read(basename, "SSH")),
                "TP", modelId(read(basename, "TP")),
                "SV", modelId(read(basename, "SV")));
    }
}
