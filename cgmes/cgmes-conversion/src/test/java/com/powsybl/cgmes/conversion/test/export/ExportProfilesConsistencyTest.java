/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.naming.NamingStrategyFactory;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.ExportersServiceLoader;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class ExportProfilesConsistencyTest extends AbstractSerDeTest {

    @Test
    void testSVSmallGridNodeBreaker() {
        Network network = importNetwork(CgmesConformity1Catalog.smallNodeBreaker().dataSource());

        ReportNode reportNodeOnlySv = ReportNode.newRootReportNode().withMessageTemplate("onlySV", "").build();
        exportProfiles(List.of("SV"), network, reportNodeOnlySv);
        assertTrue(inconsistentProfilesReported(reportNodeOnlySv));

        ReportNode reportNodeSvAndTp = ReportNode.newRootReportNode().withMessageTemplate("SVandTP", "").build();
        exportProfiles(List.of("SV", "TP"), network, reportNodeSvAndTp);
        assertFalse(inconsistentProfilesReported(reportNodeSvAndTp));
    }

    private boolean inconsistentProfilesReported(ReportNode reportNode) {
        return reportNode.getChildren().stream()
                .map(ReportNode::getMessageKey)
                .anyMatch(key -> key.equals("inconsistentProfilesTPRequired"));
    }

    private Network importNetwork(ReadOnlyDataSource dataSource) {
        Properties params = new Properties();
        params.put(CgmesImport.NAMING_STRATEGY, NamingStrategyFactory.CGMES);
        params.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        return Importers.importData("CGMES", dataSource, params);
    }

    private void exportProfiles(List<String> profiles, Network network, ReportNode reportNode) {
        Properties params = new Properties();
        params.put(CgmesExport.PROFILES, profiles);
        network.write(new ExportersServiceLoader(), "CGMES", params, tmpDir.resolve("exported"), reportNode);
    }
}
