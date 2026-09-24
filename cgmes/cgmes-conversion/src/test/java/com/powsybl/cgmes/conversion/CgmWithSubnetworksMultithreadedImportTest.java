/**
 * Copyright (c) 2026, Artelys (https://www.artelys.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conformity.CgmesConformity3Catalog;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Network sequential = importSubnetworksWithThreadCount(ds, 1, sequentialReport);
        Network parallel = importSubnetworksWithThreadCount(ds, 2, parallelReport);
        Network overSubscribed = importSubnetworksWithThreadCount(ds, 8, overSubscribedReport);

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

    @Test
    void callingThreadInterruptedDuringParallelImportThrowsPowsyblException() {
        Set<ReadOnlyDataSource> dss = new LinkedHashSet<>(List.of(new BrokenReadOnlyDataSource(), new BrokenReadOnlyDataSource()));
        Thread.currentThread().interrupt();
        try {
            PowsyblException e = assertThrows(PowsyblException.class, () -> importSubnetworksWithThreadCount(dss, 2, ReportNode.NO_OP));
            assertEquals("Interrupted while importing CGMES subnetworks", e.getMessage());
        } finally {
            assertTrue(Thread.interrupted(), "interrupt status should still be set");
        }
    }

    @Test
    void callingThreadInterruptedDuringSequentialImportThrowsPowsyblException() {
        ReadOnlyDataSource ds = CgmesConformity3Catalog.microGridBaseCaseAssembled().dataSource();
        Thread.currentThread().interrupt();
        try {
            assertThrows(PowsyblException.class, () -> importWithThreadCount(ds, 1, ReportNode.NO_OP));
        } finally {
            assertTrue(Thread.interrupted(), "interrupt status should still be set");
        }
    }

    @Test
    void oneSubnetworkImportFailureThrowsPowsyblException() {
        ReadOnlyDataSource validSubnetworkDs = CgmesConformity3Catalog.microGridBaseCaseAssembled().dataSource();
        Set<ReadOnlyDataSource> dss = Set.of(new BrokenReadOnlyDataSource(), validSubnetworkDs);
        CgmesImport cgmesImport = new CgmesImport();
        NetworkFactory networkFactory = NetworkFactory.findDefault();
        Properties importParams = new Properties();
        PowsyblException e = assertThrows(PowsyblException.class,
                () -> cgmesImport.importSubnetworks(dss, networkFactory, importParams, ReportNode.NO_OP, 2));
        assertEquals("Failed to import CGMES subnetwork", e.getMessage());
        assertEquals("CIM Namespace not found", e.getCause().getMessage());
    }

    // This test uses SlowReadOnlyDataSource which itself uses await().pollInSameThread() whose javadoc says:
    // "For safety you should always combine tests using this feature with a test framework specific timeout."
    // The test takes about 300ms, hence 2 seconds timeout should be safe.
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void oneSubnetworkImportFailureWaitsForRunningSubnetworkImports() {
        AtomicBoolean runningImportStarted = new AtomicBoolean(false);
        SlowReadOnlyDataSource slowDs = new SlowReadOnlyDataSource(CgmesConformity3Catalog.microGridBaseCaseAssembled().dataSource(), runningImportStarted);
        // The failing data source must come first: its future is the first one awaited
        Set<ReadOnlyDataSource> dss = new LinkedHashSet<>(List.of(new BrokenReadOnlyDataSource(runningImportStarted), slowDs));
        CgmesImport cgmesImport = new CgmesImport();
        NetworkFactory networkFactory = NetworkFactory.findDefault();
        Properties importParams = new Properties();
        assertThrows(PowsyblException.class,
                () -> cgmesImport.importSubnetworks(dss, networkFactory, importParams, ReportNode.NO_OP, 2));
        assertTrue(slowDs.finished, "running subnetwork import should have finished before the exception is thrown");
    }

    /**
     * A data source that blocks until its import thread is interrupted, then keeps running a little longer
     * before failing, simulating a subnetwork import that is slow to react to interruption.
     */
    private static final class SlowReadOnlyDataSource implements ReadOnlyDataSource {
        private final ReadOnlyDataSource delegate;
        private final AtomicBoolean started;
        private volatile boolean finished = false;

        private SlowReadOnlyDataSource(ReadOnlyDataSource delegate, AtomicBoolean started) {
            this.delegate = delegate;
            this.started = started;
        }

        @Override
        public String getBaseName() {
            return delegate.getBaseName();
        }

        @Override
        public boolean exists(String suffix, String ext) throws IOException {
            return delegate.exists(suffix, ext);
        }

        @Override
        public boolean exists(String fileName) throws IOException {
            return delegate.exists(fileName);
        }

        @Override
        public boolean isDataExtension(String ext) {
            return delegate.isDataExtension(ext);
        }

        @Override
        public InputStream newInputStream(String suffix, String ext) throws IOException {
            return delegate.newInputStream(suffix, ext);
        }

        @Override
        public InputStream newInputStream(String fileName) throws IOException {
            return delegate.newInputStream(fileName);
        }

        @Override
        public Set<String> listNames(String regex) throws IOException {
            started.set(true);
            await().pollInSameThread().atMost(30, TimeUnit.SECONDS).until(Thread.currentThread()::isInterrupted);
            // Clear the interrupt so that the extra work below is not cut short
            Thread.interrupted();
            await().pollDelay(200, TimeUnit.MILLISECONDS).until(() -> true);
            finished = true;
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Simulated interrupted subnetwork data reading");
        }
    }

    /**
     * A data source that always fails to read, simulating a subnetwork import failure.
     */
    private static final class BrokenReadOnlyDataSource implements ReadOnlyDataSource {
        private final AtomicBoolean failAfter;

        private BrokenReadOnlyDataSource() {
            this(new AtomicBoolean(true));
        }

        private BrokenReadOnlyDataSource(AtomicBoolean failAfter) {
            this.failAfter = failAfter;
        }

        @Override
        public String getBaseName() {
            return "broken";
        }

        @Override
        public boolean exists(String suffix, String ext) {
            return false;
        }

        @Override
        public boolean exists(String fileName) {
            return false;
        }

        @Override
        public boolean isDataExtension(String ext) {
            return false;
        }

        @Override
        public InputStream newInputStream(String suffix, String ext) throws IOException {
            throw new IOException("Simulated failure reading subnetwork data");
        }

        @Override
        public InputStream newInputStream(String fileName) throws IOException {
            throw new IOException("Simulated failure reading subnetwork data");
        }

        @Override
        public Set<String> listNames(String regex) {
            await().atMost(30, TimeUnit.SECONDS).untilTrue(failAfter);
            return new HashSet<>();
        }
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

    /**
     * Same as {@link CgmesImport#importData} with subnetworks, but bypassing the thread count limit based on available
     * processors, so that the requested thread count is actually used whatever the machine running the test.
     */
    private static Network importSubnetworksWithThreadCount(ReadOnlyDataSource ds, int threadCount, ReportNode reportNode) {
        Set<ReadOnlyDataSource> dss = new CgmesImport.MultipleGridModelChecker(ds).separate(CgmesImport.SubnetworkDefinedBy.MODELING_AUTHORITY);
        return importSubnetworksWithThreadCount(dss, threadCount, reportNode);
    }

    private static Network importSubnetworksWithThreadCount(Set<ReadOnlyDataSource> dss, int threadCount, ReportNode reportNode) {
        return Network.merge(new CgmesImport().importSubnetworks(dss, NetworkFactory.findDefault(), new Properties(), reportNode, threadCount));
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
