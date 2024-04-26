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
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.Identifiables;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.powsybl.cgmes.conversion.Conversion.CGMES_PREFIX_ALIAS_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class LegacyCommonGridModelExportTest extends AbstractSerDeTest {

    @Test
    void testExportCgmSvDependenciesOnNetworkProperties() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledSvWithMas().dataSource();
        Network network = Network.read(ds);

        // This is the legacy way of preparing dependencies for SV externally,
        // used by projects in the FARAO community
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
        System.out.println("dependencies : " + Arrays.toString(deps.toArray()));
        assertEquals(Set.of("ssh-updated-dep1", "ssh-updated-dep2", "ssh-updated-dep3", "tp-initial-dep1", "tp-initial-dep2", "tp-initial-dep3"), deps);
    }

    private static Set<String> findAll(Pattern pattern, String xml) {
        Set<String> found = new HashSet<>();
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found;
    }

    private static final Pattern REGEX_DEPENDENT_ON = Pattern.compile("<md:Model.DependentOn rdf:resource=\"(.*?)\"");
}
