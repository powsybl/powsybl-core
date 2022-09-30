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
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.export.ExportersServiceLoader;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ExportProfilesConsistencyTest extends AbstractConverterTest {

    @Test
    public void testSVSmallGridNodeBreaker() {
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
                .anyMatch(key -> key.equals("InconsistentProfilesTPRequired"));
    }

    private Network importNetwork(ReadOnlyDataSource dataSource) {
        Properties params = new Properties();
        params.put(CgmesImport.ID_MAPPING_FILE_NAMING_STRATEGY, NamingStrategyFactory.CGMES);
        return Importers.importData("CGMES", dataSource, params);
    }

    private void exportProfiles(List<String> profiles, Network network, Reporter reporter) {
        Properties params = new Properties();
        params.put(CgmesExport.PROFILES, profiles);
        Exporters.export(new ExportersServiceLoader(), "CGMES", network, params, tmpDir.resolve("exported"), reporter);
    }
}
