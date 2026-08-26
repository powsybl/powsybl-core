/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.PartialSshExport;
import com.powsybl.cgmes.conversion.export.PartialSshExport.UnsupportedChangeBehavior;
import com.powsybl.cgmes.conversion.export.SteadyStateHypothesisExport;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.events.NetworkEvent;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the response time of the partial Steady State Hypothesis export. We expect full export > many changes > one change
 *
 *
 * <p>Indicative measurements, best of ten runs after warm up, on the svedala model used here:</p>
 * <pre>
 *   1 change      0.17 ms      1.7 kB
 *   500 changes   1.60 ms       71 kB
 *   full SSH     10.10 ms      886 kB
 * </pre>
 *
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
class PartialSshExportBenchmarkTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartialSshExportBenchmarkTest.class);

    /**
     * If the export takes longer than this, something is seriously wrong.
     */
    private static final long MAX_EXPORT_TIME_MS = 100;

    /** The two ends of the range measured here, chosen far apart so the comparison has the most margin. */
    private static final int ONE_CHANGE = 1;
    private static final int MANY_CHANGES = 500;

    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 10;

    @Test
    void exportCostTracksTheChangeLogAndNotTheModel() {
        Network network = Network.read(Cgmes3Catalog.svedala().dataSource(), new Properties());

        double oneChange = bestMillis(partialExport(network, recordSwitchChanges(network, ONE_CHANGE)));
        double manyChanges = bestMillis(partialExport(network, recordSwitchChanges(network, MANY_CHANGES)));
        double fullExport = bestMillis(() -> fullExport(network));

        LOGGER.info("Partial SSH export of svedala ({} switches): {} change {} ms, {} changes {} ms, full {} ms",
                network.getSwitchCount(), ONE_CHANGE, millis(oneChange), MANY_CHANGES, millis(manyChanges),
                millis(fullExport));

        assertCheaper(manyChanges, MANY_CHANGES + " changes", fullExport, "a full steady state hypothesis");
        assertCheaper(oneChange, ONE_CHANGE + " change", manyChanges, MANY_CHANGES + " changes");
        assertTrue(manyChanges < MAX_EXPORT_TIME_MS, () -> "exporting " + MANY_CHANGES + " changes took "
                + millis(manyChanges) + " ms, more than the " + MAX_EXPORT_TIME_MS + " ms budget");
    }

    private static void assertCheaper(double millis, String what, double thanMillis, String than) {
        assertTrue(millis < thanMillis, () -> "a partial export of " + what + " took " + millis(millis)
                + " ms, no less than the " + millis(thanMillis) + " ms of " + than);
    }

    /** The shortest run of the given export, which is the measure least disturbed by the rest of the machine. */
    private static double bestMillis(Supplier<String> export) {
        for (int run = 0; run < WARMUP_RUNS; run++) {
            export.get();
        }
        long bestNanos = Long.MAX_VALUE;
        for (int run = 0; run < MEASURED_RUNS; run++) {
            long startNanos = System.nanoTime();
            export.get();
            bestNanos = Math.min(bestNanos, System.nanoTime() - startNanos);
        }
        return bestNanos / 1e6;
    }

    private static Supplier<String> partialExport(Network network, List<NetworkEvent> events) {
        return () -> PartialSshExport.toString(network, events, UnsupportedChangeBehavior.FAIL);
    }

    /**
     * A full steady state hypothesis of the same network, written the same way, so that the comparison is between
     * the two exports and not between two ways of writing a document. The export context is built inside both, as
     * it is part of what a caller pays.
     */
    private static String fullExport(Network network) {
        StringWriter out = new StringWriter();
        try {
            SteadyStateHypothesisExport.write(network, XmlUtil.initializeWriter(true, "    ", out),
                    new CgmesExportContext(network));
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
        return out.toString();
    }

    /** Toggle the given number of switches and return the changes that were recorded for it. */
    private static List<NetworkEvent> recordSwitchChanges(Network network, int changeCount) {
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        network.addListener(recorder);
        try {
            List<Switch> switches = new ArrayList<>();
            network.getSwitchStream().forEach(switches::add);
            for (int change = 0; change < changeCount; change++) {
                Switch sw = switches.get(change % switches.size());
                sw.setOpen(!sw.isOpen());
            }
            return List.copyOf(recorder.getEvents());
        } finally {
            network.removeListener(recorder);
        }
    }

    private static String millis(double millis) {
        return String.format("%.2f", millis);
    }
}
