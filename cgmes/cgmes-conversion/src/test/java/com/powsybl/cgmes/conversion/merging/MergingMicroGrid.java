/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.merging;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.test.ExpectedFlows;
import com.powsybl.cgmes.conformity.test.ExpectedFlows.ExpectedFlow;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class MergingMicroGrid {

    private static final double TOLERANCE = 0.1;

    private static ExpectedFlows expectedFlows;

    private static final Logger LOG = LoggerFactory.getLogger(MergingMicroGrid.class);

    @BeforeClass
    public static void setup() {
        expectedFlows = CgmesConformity1Catalog.expectedFlowsMicroGridBaseCase();
    }

    @Test
    public void testFlowsAssembledWithBoundariesKeptInIIDM() {
        boolean mustContainMergedLines = false;
        checkFlows(assembledKeepingBoundaries(), mustContainMergedLines);
    }

    @Test
    public void testFlowsAssembledBoundariesRemovedLinesMergedInCgmesImport() {
        boolean mustContainMergedLines = true;
        checkFlows(assembledNoBoundaries(), mustContainMergedLines);
    }

    @Test
    public void testFlowsMergingViewMergedLines() {
        boolean mustContainMergedLines = true;
        checkFlows(mergingView(), mustContainMergedLines);
    }

    @Test
    public void testMergingViewMergedLinesNotModifiable() {
        for (Line l : mergingView().getLines()) {
            if (isMergedLine(l)) {
                checkModificationThrowsException(l::setR);
                checkModificationThrowsException(l::setX);
                checkModificationThrowsException(l::setG1);
                checkModificationThrowsException(l::setB1);
                checkModificationThrowsException(l::setG2);
                checkModificationThrowsException(l::setB2);
            }
        }
    }

    @Test
    public void testFlowsDestructiveMerge() {
        boolean mustContainMergedLines = true;
        checkFlows(destructiveMerge(), mustContainMergedLines);
    }

    private static void checkFlows(Network network, boolean mustContainMergedLines) {
        ConversionTester.invalidateFlows(network);
        ConversionTester.computeMissingFlows(network);
        assertEquals(0, calcDiffExpectedFlows(network, mustContainMergedLines), TOLERANCE);
    }

    private static void checkModificationThrowsException(Consumer<Double> setter) {
        try {
            setter.accept(1.0);
            fail();
        } catch (PowsyblException x) {
            assertTrue(x.getMessage().contains("cannot be modified"));
        }
    }

    private static Network mergingView() {
        Properties iparams = new Properties();
        iparams.put(CgmesImport.PROFILE_USED_FOR_INITIAL_STATE_VALUES, "SV");
        Network be = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), iparams);
        Network nl = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseNL().dataSource(), iparams);
        MergingView merged = MergingView.create("BE-NL", "CGMES");
        merged.merge(be, nl);
        return merged;
    }

    private static Network destructiveMerge() {
        Properties iparams = new Properties();
        iparams.put(CgmesImport.PROFILE_USED_FOR_INITIAL_STATE_VALUES, "SV");
        Network be = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), iparams);
        Network nl = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseNL().dataSource(), iparams);
        be.merge(nl);
        return be;
    }

    private static Network assembledKeepingBoundaries() {
        Properties iparams = new Properties();
        iparams.put(CgmesImport.PROFILE_USED_FOR_INITIAL_STATE_VALUES, "SV");
        // Convert boundary to analyze flows at x-nodes
        iparams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        return Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(),
                iparams);
    }

    private static Network assembledNoBoundaries() {
        Properties iparams = new Properties();
        iparams.put(CgmesImport.PROFILE_USED_FOR_INITIAL_STATE_VALUES, "SV");
        iparams.put(CgmesImport.CONVERT_BOUNDARY, "false");
        return Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(),
                iparams);
    }

    private static double calcDiffExpectedFlows(Network network, boolean mustContainMergedLines) {
        double diff = 0;
        int countMergedLines = 0;
        for (Line l : network.getLines()) {
            double diffl = 0;
            ExpectedFlow[] expected = null;
            if (isMergedLine(l)) {
                countMergedLines++;

                // We need to obtain expected flows for merged line
                // using a method that is valid for:
                // - merging view and for
                // - merged lines in CGMES import
                // We can not use CGMES terminal identifiers
                // We assume the identifiers of individual lines are present
                // in the id of the merged line, separated by a " + ",
                // but they could be in a different order of the terminals assigned
                // The individual id on the left of the " + " could be on Terminal2

                // We check both options and return the one that gives the lowest difference

                logExpectedFlowCandidatesForMergedLine(l);
                ExpectedFlow[] expected12 = findExpectedFlowsMergedLine(l, true);
                ExpectedFlow[] expected21 = findExpectedFlowsMergedLine(l, false);
                double diff12 = diffFlow(expected12, l);
                double diff21 = diffFlow(expected21, l);
                if (diff12 < diff21) {
                    diffl = diff12;
                    expected = expected12;
                } else {
                    diffl = diff21;
                    expected = expected21;
                }
            } else {
                expected = new ExpectedFlow[2];
                expected[0] = expectedFlows.findBestCandidate(l, Branch.Side.ONE);
                expected[1] = expectedFlows.findBestCandidate(l, Branch.Side.TWO);
                diffl = diffFlow(expected, l);
            }
            logDiffFlow(expected, l);
            diff += diffl;
        }
        if (mustContainMergedLines && countMergedLines == 0) {
            throw new PowsyblException("No merged lines were found, and network must contain some of them");
        }
        return diff;
    }

    private static boolean isMergedLine(Line line) {
        return line.getId().contains(" + ");
    }

    private static ExpectedFlow[] findExpectedFlowsMergedLine(Line mergedLine, boolean assumeIdOrder12) {
        String[] igmLines = mergedLine.getId().split(" \\+ ");
        String id1 = assumeIdOrder12 ? igmLines[0] : igmLines[1];
        String id2 = assumeIdOrder12 ? igmLines[1] : igmLines[0];

        ExpectedFlow[] expected = new ExpectedFlow[2];
        expected[0] = expectedFlows.findBestCandidate(id1, mergedLine.getTerminal1().getP());
        expected[1] = expectedFlows.findBestCandidate(id2, mergedLine.getTerminal2().getP());
        return expected;
    }

    private static double diffFlow(ExpectedFlow[] flows, Branch<?> b) {
        return diffFlow(flows[0], b.getTerminal1()) + diffFlow(flows[1], b.getTerminal2());
    }

    private static double diffFlow(ExpectedFlow f, Terminal t) {
        return diffFlow(f.p, t.getP()) + diffFlow(f.q, t.getQ());
    }

    private static double diffFlow(double expected, double actual) {
        return Math.abs(expected - actual);
    }

    private static void logExpectedFlowCandidatesForMergedLine(Line mergedLine) {
        if (LOG.isDebugEnabled()) {
            String[] igmLines = mergedLine.getId().split(" \\+ ");
            LOG.debug("Merged line {}, find expected flows from IGM lines", mergedLine.getNameOrId());
            LOG.debug("  IGM line 1 : {}", igmLines[0]);
            LOG.debug("  IGM line 2 : {}", igmLines[1]);
            expectedFlows.get(igmLines[0]).forEach(f -> LOG.debug("  IGM P1 expected {} (candidate)", formatP(f.p)));
            expectedFlows.get(igmLines[1]).forEach(f -> LOG.debug("  IGM P2 expected {} (candidate)", formatP(f.p)));
            LOG.debug("  CGM P1 actual   {}", formatP(mergedLine.getTerminal1().getP()));
            LOG.debug("  CGM P2 actual   {}", formatP(mergedLine.getTerminal2().getP()));
        }
    }

    private static String formatP(double p) {
        return String.format("%8.2f", p);
    }

    private static void logDiffFlow(ExpectedFlow[] flows, Branch<?> b) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Line {}, diff with expected flows {}", b.getNameOrId(), b.getId());
            LOG.debug("        expected  actual--  diff----");
            LOG.debug("    P1  {}  {}  {}", formatP(flows[0].p), formatP(b.getTerminal1().getP()), formatP(Math.abs(flows[0].p - b.getTerminal1().getP())));
            LOG.debug("    Q1  {}  {}  {}", formatP(flows[0].q), formatP(b.getTerminal1().getQ()), formatP(Math.abs(flows[0].q - b.getTerminal1().getQ())));
            LOG.debug("    P2  {}  {}  {}", formatP(flows[1].p), formatP(b.getTerminal2().getP()), formatP(Math.abs(flows[1].p - b.getTerminal2().getP())));
            LOG.debug("    Q2  {}  {}  {}", formatP(flows[1].q), formatP(b.getTerminal2().getQ()), formatP(Math.abs(flows[1].q - b.getTerminal2().getQ())));
            LOG.debug("    tot ________  ________  {}", formatP(diffFlow(flows[0], b.getTerminal1()) + diffFlow(flows[1], b.getTerminal2())));
        }
    }
}
