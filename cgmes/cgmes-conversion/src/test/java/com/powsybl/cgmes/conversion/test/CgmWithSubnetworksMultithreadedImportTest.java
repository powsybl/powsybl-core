/**
 * Copyright (c) 2026, Artelys (https://www.artelys.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity3Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class CgmWithSubnetworksMultithreadedImportTest {

    private static final String EXPECTED_REPORT = """
            + Test reports
               + Reading CGMES Triplestore
                  Instance file 20210325T1530Z_1D_ASSEMBLED_TP_001.xml
                  Instance file 20210325T1530Z_1D_BE_EQ_001.xml
                  Instance file 20210325T1530Z_1D_BE_SSH_001.xml
                  Instance file 20171002T0930Z_ENTSO-E_EQ_BD_2.xml
                  Instance file 20210325T1530Z_1D_ASSEMBLED_SV_001.xml
               + Importing CGMES file(s) with basename '20210325T1530Z_1D_BE'
                  Applying preprocessors.
                  Building mappings.
                  Converting Substation.
                  Converting VoltageLevel.
                  Converting ConnectivityNode.
                  Converting BusbarSection.
                  Converting Ground.
                  Converting EnergyConsumer.
                  Converting EnergySource.
                  Converting EquivalentInjection.
                  Converting ExternalNetworkInjection.
                  Converting ShuntCompensator.
                  Converting EquivalentShunt.
                  Converting StaticVarCompensator.
                  Converting AsynchronousMachine.
                  Converting SynchronousMachine.
                  Converting Switch.
                  Converting ACLineSegment.
                  Converting EquivalentBranch.
                  Converting SeriesCompensator.
                  Converting PowerTransformer.
                  Converting equipments at boundaries.
                  Converting DC network.
                  Converting OperationalLimit.
                  Converting ControlArea.
                  Converting TieFlow.
                  Converting RegulatingControl.
                  Applying postprocessors.
                  CGMES network urn:uuid:095c6b30-255d-40d5-85fe-2c9fe6c9846d is imported.
                  Converting during update Terminal.
                  Converting during update TIE_LINE.
                  Converting during update SvInjection.
                  Updating SWITCH.
                  Updating LOAD.
                  Updating GENERATOR.
                  Updating LINE.
                  Updating TWO_WINDINGS_TRANSFORMER.
                  Updating THREE_WINDINGS_TRANSFORMER.
                  Updating STATIC_VAR_COMPENSATOR.
                  Updating SHUNT_COMPENSATOR.
                  Updating HVDC_LINE.
                  Updating BOUNDARY_LINE.
                  Fixing issues with boundary lines.
                  Updating VOLTAGE_LEVEL.
                  Updating GROUND.
                  Updating AREA.
                  Setting voltages and angles.
                  Running validation checks on IIDM network urn:uuid:095c6b30-255d-40d5-85fe-2c9fe6c9846d
               + Reading CGMES Triplestore
                  Instance file 20210325T1530Z_1D_NL_SSH_001.xml
                  Instance file 20210325T1530Z_1D_NL_EQ_001.xml
                  Instance file 20171002T0930Z_ENTSO-E_EQ_BD_2.xml
               + Importing CGMES file(s) with basename '20210325T1530Z_1D_BE'
                  Applying preprocessors.
                  Building mappings.
                  Converting Substation.
                  Converting VoltageLevel.
                  Converting ConnectivityNode.
                  Converting BusbarSection.
                  Converting Ground.
                  Converting EnergyConsumer.
                  Converting EnergySource.
                  Converting EquivalentInjection.
                  Converting ExternalNetworkInjection.
                  Converting ShuntCompensator.
                  Converting EquivalentShunt.
                  Converting StaticVarCompensator.
                  Converting AsynchronousMachine.
                  Converting SynchronousMachine.
                  Converting Switch.
                  Converting ACLineSegment.
                  Converting EquivalentBranch.
                  Converting SeriesCompensator.
                  Converting PowerTransformer.
                  Converting equipments at boundaries.
                  Converting DC network.
                  Converting OperationalLimit.
                  Converting ControlArea.
                  Converting TieFlow.
                  Converting RegulatingControl.
                  Applying postprocessors.
                  CGMES network urn:uuid:87da6373-3b6c-47a2-9493-1918a8d9df61 is imported.
                  Converting during update Terminal.
                  Converting during update TIE_LINE.
                  Converting during update SvInjection.
                  Updating SWITCH.
                  Updating LOAD.
                  Updating GENERATOR.
                  Updating LINE.
                  Updating TWO_WINDINGS_TRANSFORMER.
                  Updating THREE_WINDINGS_TRANSFORMER.
                  Updating STATIC_VAR_COMPENSATOR.
                  Updating SHUNT_COMPENSATOR.
                  Updating HVDC_LINE.
                  Updating BOUNDARY_LINE.
                  Fixing issues with boundary lines.
                  Updating VOLTAGE_LEVEL.
                  Updating GROUND.
                  Updating AREA.
                  Setting voltages and angles.
                  Running validation checks on IIDM network urn:uuid:87da6373-3b6c-47a2-9493-1918a8d9df61
            """;

    @Test
    void concurrentSubnetworkImportMatchesSequentialImport() {
        ReadOnlyDataSource ds = CgmesConformity3Catalog.microGridBaseCaseAssembled().dataSource();

        ReportNode sequentialReport = newReportNode();
        ReportNode parallelReport = newReportNode();
        // More threads than subnetworks must be clamped, not fail
        ReportNode overSubscribedReport = newReportNode();
        Network sequential = importWithThreadCount(ds, 1, sequentialReport);
        Network parallel = importWithThreadCount(ds, 2, parallelReport);
        Network overSubscribed = importWithThreadCount(ds, 8, overSubscribedReport);

        assertEquals(subnetworkIds(sequential), subnetworkIds(parallel));
        assertEquals(subnetworkIds(sequential), subnetworkIds(overSubscribed));
        for (String subnetworkId : subnetworkIds(sequential)) {
            assertEquals(sequential.getSubnetwork(subnetworkId).getIdentifiables().size(),
                    parallel.getSubnetwork(subnetworkId).getIdentifiables().size());
            assertEquals(sequential.getSubnetwork(subnetworkId).getIdentifiables().size(),
                    overSubscribed.getSubnetwork(subnetworkId).getIdentifiables().size());
        }

        // The report content (including per-subnetwork child ordering) must not depend on the thread count.
        assertEquals(EXPECTED_REPORT, print(sequentialReport));
        assertEquals(EXPECTED_REPORT, print(parallelReport));
        assertEquals(EXPECTED_REPORT, print(overSubscribedReport));
    }

    private static ReportNode newReportNode() {
        return ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("test")
                .build();
    }

    private static Network importWithThreadCount(ReadOnlyDataSource ds, int threadCount, ReportNode reportNode) {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS_THREAD_COUNT, String.valueOf(threadCount));
        return Network.read(ds, importParams, reportNode);
    }

    private static List<String> subnetworkIds(Network network) {
        return network.getSubnetworks().stream().map(Network::getId).sorted().toList();
    }

    private static String print(ReportNode reportNode) {
        StringWriter sw = new StringWriter();
        try {
            reportNode.print(sw);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return TestUtil.normalizeLineSeparator(sw.toString());
    }
}
