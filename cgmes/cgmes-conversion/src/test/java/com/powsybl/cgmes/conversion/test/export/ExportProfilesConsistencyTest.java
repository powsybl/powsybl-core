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
import com.powsybl.cgmes.conversion.NamingStrategyFactory;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.ReporterModel;
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

        ReporterModel reporterOnlySv = new ReporterModel("onlySV", "");
        exportProfiles(List.of("SV"), network, reporterOnlySv);
        assertTrue(inconsistentProfilesReported(reporterOnlySv));

        ReporterModel reporterSvAndTp = new ReporterModel("SVandTP", "");
        exportProfiles(List.of("SV", "TP"), network, reporterSvAndTp);
        assertFalse(inconsistentProfilesReported(reporterSvAndTp));
    }

    private boolean inconsistentProfilesReported(ReporterModel reporter) {
        return reporter.getReports().stream()
                .map(Report::getReportKey)
                .anyMatch(key -> key.equals("inconsistentProfilesTPRequired"));
    }

    private Network importNetwork(ReadOnlyDataSource dataSource) {
        Properties params = new Properties();
        params.put(CgmesImport.NAMING_STRATEGY, NamingStrategyFactory.CGMES);
        return Importers.importData("CGMES", dataSource, params);
    }

    private void exportProfiles(List<String> profiles, Network network, Reporter reporter) {
        Properties params = new Properties();
        params.put(CgmesExport.PROFILES, profiles);
        network.write(new ExportersServiceLoader(), "CGMES", params, tmpDir.resolve("exported"), reporter);
    }
}
