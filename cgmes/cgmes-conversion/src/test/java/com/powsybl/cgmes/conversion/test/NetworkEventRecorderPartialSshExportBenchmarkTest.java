/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conversion.export.NetworkEventRecorderSshExport;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.events.NetworkEvent;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the response time of the partial Steady State Hypothesis export, which is what makes it usable for
 * exchanging a business process result between processes: exporting a small change log has to stay far cheaper
 * than serializing the whole grid model.
 *
 * <p>The measured cost has two parts. The part that depends on the change log is a few microseconds per change.
 * The fixed part is the construction of the export context, which walks the substations and the voltage levels of
 * the network and therefore grows with the size of the model, not with the number of changes. On the grids used
 * here the total stays around three milliseconds, two orders of magnitude below the budget asserted below.</p>
 *
 * <p>Indicative measurements, best of ten runs after warm up, on a 2 342 switch model:</p>
 * <pre>
 *   1 change     0.25 ms
 *   500 changes  2.22 ms
 * </pre>
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
class NetworkEventRecorderPartialSshExportBenchmarkTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkEventRecorderPartialSshExportBenchmarkTest.class);

    /**
     * Generous enough not to be flaky on a loaded continuous integration machine, while still catching a change
     * that would make the export scale with something other than the size of the change log.
     */
    private static final long MAX_EXPORT_TIME_MS = 100;

    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 10;

    @Test
    void smallChangeLogOnMediumGridIsFast() {
        assertExportTimeUnderBudget(Cgmes3Catalog.smallGrid().dataSource(), "smallGrid", 500);
    }

    @Test
    void smallChangeLogOnLargerGridIsFast() {
        assertExportTimeUnderBudget(Cgmes3Catalog.svedala().dataSource(), "svedala", 500);
    }

    @Test
    void singleChangeIsFast() {
        assertExportTimeUnderBudget(Cgmes3Catalog.svedala().dataSource(), "svedala", 1);
    }

    private static void assertExportTimeUnderBudget(ReadOnlyDataSource dataSource, String name, int changeCount) {
        Network network = Network.read(dataSource, new Properties());
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        network.addListener(recorder);
        recordSwitchChanges(network, changeCount);
        List<NetworkEvent> events = List.copyOf(recorder.getEvents());

        for (int run = 0; run < WARMUP_RUNS; run++) {
            NetworkEventRecorderSshExport.toString(network, events);
        }

        long bestNanos = Long.MAX_VALUE;
        for (int run = 0; run < MEASURED_RUNS; run++) {
            long startNanos = System.nanoTime();
            NetworkEventRecorderSshExport.toString(network, events);
            bestNanos = Math.min(bestNanos, System.nanoTime() - startNanos);
        }

        double bestMillis = bestNanos / 1e6;
        LOGGER.info("Partial SSH export of {} changes on {} ({} switches): {} ms",
                events.size(), name, network.getSwitchCount(), String.format("%.2f", bestMillis));
        assertTrue(bestMillis < MAX_EXPORT_TIME_MS,
                () -> "exporting " + changeCount + " changes on " + name + " took " + bestMillis
                        + " ms, more than the " + MAX_EXPORT_TIME_MS + " ms budget");
    }

    private static void recordSwitchChanges(Network network, int changeCount) {
        List<Switch> switches = new ArrayList<>();
        network.getSwitchStream().forEach(switches::add);
        for (int change = 0; change < changeCount; change++) {
            Switch sw = switches.get(change % switches.size());
            sw.setOpen(!sw.isOpen());
        }
    }
}
