/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.junit.Test;

import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractValidationFormatterWriterTest {

    protected final String branchId = "branchId";
    protected final String otherBranchId = "otherBranchId";
    protected final double p1 = 39.5056;
    protected final double p1Calc = 39.5058;
    protected final double q1 = -3.72344;
    protected final double q1Calc = -3.72348;
    protected final double p2 = -39.5122;
    protected final double p2Calc = -39.5128;
    protected final double q2 = 3.7746;
    protected final double q2Calc = 3.7742;
    protected final double r = 0.04;
    protected final double x = 0.423;
    protected final double g1 = 0.0;
    protected final double g2 = 0.0;
    protected final double b1 = 0.0;
    protected final double b2 = 0.0;
    protected final double rho1 = 1;
    protected final double rho2 = 11.249999728;
    protected final double alpha1 = 0.0;
    protected final double alpha2 = 0.0;
    protected final double u1 = 236.80258178710938;
    protected final double u2 = 21.04814910888672;
    protected final double theta1 = 0.1257718437996544;
    protected final double theta2 = 0.12547118123496284;
    protected final double z = Math.hypot(r, x);
    protected final double y = 1 / z;
    protected final double ksi = Math.atan2(r, x);
    protected final boolean connected1 = true;
    protected final boolean connected2 = true;
    protected final boolean mainComponent1 = true;
    protected final boolean mainComponent2 = true;
    protected final boolean validated = true;

    protected final String generatorId = "generatorId";
    protected final String otherGeneratorId = "otherGeneratorId";
    protected final double p = -39.5056;
    protected final double q = 3.72344;
    protected final double v = 380;
    protected final double targetP = 39.5056;
    protected final double targetQ = -3.72344;
    protected final double targetV = 380;
    protected final boolean connected = true;
    protected final boolean voltageRegulatorOn = true;
    protected final double minP = 25;
    protected final double maxP = 45;
    protected final double minQ = -10;
    protected final double maxQ = 0;

    protected final String busId = "busId";
    protected final String otherBusId = "otherBusId";
    protected final double incomingP = -37.2287;
    protected final double incomingQ = -174.383;
    protected final double loadP = 37.2286;
    protected final double loadQ = 174.38244;
    protected final double genP = -2020;
    protected final double genQ = 91.54;
    protected final double shuntP = 0;
    protected final double shuntQ = 175.8437;
    protected final double svcP = 0;
    protected final double svcQ = 0;
    protected final double vscCSP = 0;
    protected final double vscCSQ = 0;
    protected final double lineP = 1982.7713;
    protected final double lineQ = -441.7662;
    protected final double danglingLineP = 0;
    protected final double danglingLineQ = 0;
    protected final double twtP = 0;
    protected final double twtQ = 0;
    protected final double tltP = 0;
    protected final double tltQ = 0;

    protected final String svcId = "svcId";
    protected final String otherSvcId = "otherSvcId";
    protected final double reactivePowerSetpoint = -3.72344;
    protected final double voltageSetpoint = 380;
    protected final RegulationMode regulationMode = RegulationMode.VOLTAGE;
    protected final double bMin = -10;
    protected final double bMax = 0;

    protected final String shuntId = "shuntId";
    protected final String otherShuntId = "otherShuntId";
    protected final double expectedQ = 3.724;
    protected int currentSectionCount = 0;
    protected int maximumSectionCount = 1;
    protected double bPerSection = -0.16;
    protected double qMax = -144.4;
    protected final double nominalV = 380;

    protected final String twtId = "twtId";
    protected final String otherTwtId = "otherTwtId";
    protected final double error = 0.000243738;
    protected final double upIncrement = 0.00944448;
    protected final double downIncrement = -0.00834519;
    protected final double rho = 1.034;
    protected final double rhoPreviousStep = 1.043;
    protected final double rhoNextStep = 1.024;
    protected final int tapPosition = 8;
    protected final int lowTapPosition = 0;
    protected final int highTapPosition = 30;
    protected final double twtTargetV = 92.7781;
    protected final Side regulatedSide = Side.ONE;
    protected final double twtV = 92.8007;
    protected final boolean mainComponent = true;

    @Test
    public void testFlows() throws Exception {
        testFlows(getFlowsContent(), false, false, branchId, null);
    }

    protected abstract String getFlowsContent();

    @Test
    public void testFlowsVerbose() throws Exception {
        testFlows(getFlowsVerboseContent(), true, false, branchId, null);
    }

    protected abstract String getFlowsVerboseContent();

    @Test
    public void testFlowsCompare() throws Exception {
        testFlows(getFlowsCompareContent(), false, true, branchId, branchId);
    }

    protected abstract String getFlowsCompareContent();

    @Test
    public void testFlowsCompareDifferentIds() throws Exception {
        testFlows(getFlowsCompareDifferentIdsContent(), false, true, branchId, otherBranchId);
    }

    protected abstract String getFlowsCompareDifferentIdsContent();

    @Test
    public void testFlowsCompareVerbose() throws Exception {
        testFlows(getFlowsCompareVerboseContent(), true, true, branchId, branchId);
    }

    protected abstract String getFlowsCompareVerboseContent();

    @Test
    public void testFlowsCompareDifferentIdsVerbose() throws Exception {
        testFlows(getFlowsCompareDifferentIdsVerboseContent(), true, true, branchId, otherBranchId);
    }

    protected abstract String getFlowsCompareDifferentIdsVerboseContent();

    protected void testFlows(String flowsContent, boolean verbose, boolean compareResults, String branchId1, String branchId2) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter flowsWriter = getFlowsValidationFormatterCsvWriter(config, writer, verbose, compareResults)) {
            flowsWriter.write(branchId1, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2,
                              alpha1, alpha2, u1, u2, theta1, theta2, z, y, ksi, connected1, connected2, mainComponent1,
                              mainComponent2, validated);
            flowsWriter.setValidationCompleted();
            if (compareResults) {
                flowsWriter.write(branchId2, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2,
                                  alpha1, alpha2, u1, u2, theta1, theta2, z, y, ksi, connected1, connected2, mainComponent1,
                                  mainComponent2, validated);
                flowsWriter.setValidationCompleted();
            }
            assertEquals(flowsContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getFlowsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults);

    @Test
    public void testGenerators() throws Exception {
        testGenerators(getGeneratorsContent(), false, false, generatorId, null);
    }

    protected abstract String getGeneratorsContent();

    @Test
    public void testGeneratorsVerbose() throws Exception {
        testGenerators(getGeneratorsVerboseContent(), true, false, generatorId, null);
    }

    protected abstract String getGeneratorsVerboseContent();

    @Test
    public void testGeneratorsCompare() throws Exception {
        testGenerators(getGeneratorsCompareContent(), false, true, generatorId, generatorId);
    }

    protected abstract String getGeneratorsCompareContent();

    @Test
    public void testGeneratorsCompareDifferentIds() throws Exception {
        testGenerators(getGeneratorsCompareDifferentIdsContent(), false, true, generatorId, otherGeneratorId);
    }

    protected abstract String getGeneratorsCompareDifferentIdsContent();

    @Test
    public void testGeneratorsCompareVerbose() throws Exception {
        testGenerators(getGeneratorsCompareVerboseContent(), true, true, generatorId, generatorId);
    }

    protected abstract String getGeneratorsCompareVerboseContent();

    @Test
    public void testGeneratorsCompareDifferentIdsVerbose() throws Exception {
        testGenerators(getGeneratorsCompareDifferentIdsVerboseContent(), true, true, generatorId, otherGeneratorId);
    }

    protected abstract String getGeneratorsCompareDifferentIdsVerboseContent();

    protected void testGenerators(String generatorsContent, boolean verbose, boolean compareResults, String generatorId1, String generatorId2) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter generatorsWriter = getGeneratorsValidationFormatterCsvWriter(config, writer, verbose, compareResults)) {
            generatorsWriter.write(generatorId1, p, q, v, targetP, targetQ, targetV, connected, voltageRegulatorOn, minP, maxP, minQ, maxQ, mainComponent, validated);
            generatorsWriter.setValidationCompleted();
            if (compareResults) {
                generatorsWriter.write(generatorId2, p, q, v, targetP, targetQ, targetV, connected, voltageRegulatorOn, minP, maxP, minQ, maxQ, mainComponent, validated);
                generatorsWriter.setValidationCompleted();
            }
            assertEquals(generatorsContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getGeneratorsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults);

    @Test
    public void testBuses() throws Exception {
        testBuses(getBusesContent(), false, false, busId, null);
    }

    protected abstract String getBusesContent();

    @Test
    public void testBusesVerbose() throws Exception {
        testBuses(getBusesVerboseContent(), true, false, busId, null);
    }

    protected abstract String getBusesVerboseContent();

    @Test
    public void testBusesCompare() throws Exception {
        testBuses(getBusesCompareContent(), false, true, busId,  busId);
    }

    protected abstract String getBusesCompareContent();

    @Test
    public void testBusesCompareDifferentIds() throws Exception {
        testBuses(getBusesCompareDifferentIdsContent(), false, true, busId,  otherBusId);
    }

    protected abstract String getBusesCompareDifferentIdsContent();

    @Test
    public void testBusesCompareVerbose() throws Exception {
        testBuses(getBusesCompareVerboseContent(), true, true, busId,  busId);
    }

    protected abstract String getBusesCompareVerboseContent();

    @Test
    public void testBusesCompareDifferentIdsVerbose() throws Exception {
        testBuses(getBusesCompareDifferentIdsVerboseContent(), true, true, busId,  otherBusId);
    }

    protected abstract String getBusesCompareDifferentIdsVerboseContent();

    private void testBuses(String busesContent, boolean verbose, boolean compareResults, String busId1, String busId2) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter busesWriter = getBusesValidationFormatterCsvWriter(config, writer, verbose, compareResults)) {
            busesWriter.write(busId1, incomingP, incomingQ, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                              lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, validated);
            busesWriter.setValidationCompleted();
            if (compareResults) {
                busesWriter.write(busId2, incomingP, incomingQ, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                  lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, validated);
                busesWriter.setValidationCompleted();
            }
            assertEquals(busesContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getBusesValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults);

    @Test
    public void testSvcs() throws Exception {
        testSvcs(getSvcsContent(), false, false, svcId, null);
    }

    protected abstract String getSvcsContent();

    @Test
    public void testSvcsVerbose() throws Exception {
        testSvcs(getSvcsVerboseContent(), true, false, svcId, null);
    }

    protected abstract String getSvcsVerboseContent();

    @Test
    public void testSvcsConpare() throws Exception {
        testSvcs(getSvcsCompareContent(), false, true, svcId, svcId);
    }

    protected abstract String getSvcsCompareContent();

    @Test
    public void testSvcsConpareDifferentIds() throws Exception {
        testSvcs(getSvcsCompareDifferentIdsContent(), false, true, svcId, otherSvcId);
    }

    protected abstract String getSvcsCompareDifferentIdsContent();

    @Test
    public void testSvcsConpareVerbose() throws Exception {
        testSvcs(getSvcsCompareVerboseContent(), true, true, svcId, svcId);
    }

    protected abstract String getSvcsCompareVerboseContent();

    @Test
    public void testSvcsConpareDifferentIdsVerbose() throws Exception {
        testSvcs(getSvcsCompareDifferentIdsVerboseContent(), true, true, svcId, otherSvcId);
    }

    protected abstract String getSvcsCompareDifferentIdsVerboseContent();

    protected void testSvcs(String svcsContent, boolean verbose, boolean compareResults, String svcId1, String svcId2) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter svcsWriter = getSvcsValidationFormatterCsvWriter(config, writer, verbose, compareResults)) {
            svcsWriter.write(svcId1, p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, verbose, regulationMode, bMin, bMax, mainComponent, validated);
            svcsWriter.setValidationCompleted();
            if (compareResults) {
                svcsWriter.write(svcId2, p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, verbose, regulationMode, bMin, bMax, mainComponent, validated);
                svcsWriter.setValidationCompleted();
            }
            assertEquals(svcsContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getSvcsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults);

    @Test
    public void testShunts() throws Exception {
        testShunts(getShuntsContent(), false, false, shuntId, null);
    }

    protected abstract String getShuntsContent();

    @Test
    public void testShuntsVerbose() throws Exception {
        testShunts(getShuntsVerboseContent(), true, false, shuntId,  null);
    }

    protected abstract String getShuntsVerboseContent();

    @Test
    public void testShuntsCompare() throws Exception {
        testShunts(getShuntsCompareContent(), false, true, shuntId, shuntId);
    }

    protected abstract String getShuntsCompareContent();

    @Test
    public void testShuntsCompareDifferentIds() throws Exception {
        testShunts(getShuntsCompareDifferentIdsContent(), false, true, shuntId, otherShuntId);
    }

    protected abstract String getShuntsCompareDifferentIdsContent();

    @Test
    public void testShuntsCompareVerbose() throws Exception {
        testShunts(getShuntsCompareVerboseContent(), true, true, shuntId, shuntId);
    }

    protected abstract String getShuntsCompareVerboseContent();

    @Test
    public void testShuntsCompareDifferentIdsVerbose() throws Exception {
        testShunts(getShuntsCompareDifferentIdsVerboseContent(), true, true, shuntId, otherShuntId);
    }

    protected abstract String getShuntsCompareDifferentIdsVerboseContent();

    protected void testShunts(String shuntsContent, boolean verbose, boolean compareResults, String shuntId1, String shuntId2) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter shuntsWriter = getShuntsValidationFormatterCsvWriter(config, writer, verbose, compareResults)) {
            shuntsWriter.write(shuntId1, q, expectedQ, p, currentSectionCount, maximumSectionCount, bPerSection, v, connected, qMax, nominalV, mainComponent, validated);
            shuntsWriter.setValidationCompleted();
            if (compareResults) {
                shuntsWriter.write(shuntId2, q, expectedQ, p, currentSectionCount, maximumSectionCount, bPerSection, v, connected, qMax, nominalV, mainComponent, validated);
                shuntsWriter.setValidationCompleted();
            }
            assertEquals(shuntsContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getShuntsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults);

    @Test
    public void testTwts() throws Exception {
        testTwts(getTwtsContent(), false, false, twtId, null);
    }

    protected abstract String getTwtsContent();

    @Test
    public void testTwtsVerbose() throws Exception {
        testTwts(getTwtsVerboseContent(), true, false, twtId, null);
    }

    protected abstract String getTwtsVerboseContent();

    @Test
    public void testTwtsCompare() throws Exception {
        testTwts(getTwtsCompareContent(), false, true, twtId, twtId);
    }

    protected abstract String getTwtsCompareContent();

    @Test
    public void testTwtsCompareDifferentIds() throws Exception {
        testTwts(getTwtsCompareDifferentIdsContent(), false, true, twtId, otherTwtId);
    }

    protected abstract String getTwtsCompareDifferentIdsContent();

    @Test
    public void testTwtsCompareVerbose() throws Exception {
        testTwts(getTwtsCompareVerboseContent(), true, true, twtId, twtId);
    }

    protected abstract String getTwtsCompareVerboseContent();

    @Test
    public void testTwtsCompareDifferentIdsVerbose() throws Exception {
        testTwts(getTwtsCompareDifferentIdsVerboseContent(), true, true, twtId, otherTwtId);
    }

    protected abstract String getTwtsCompareDifferentIdsVerboseContent();

    protected void testTwts(String twtsContent, boolean verbose, boolean compareResults, String twtId1, String twtId2) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter twtsWriter = getTwtsValidationFormatterCsvWriter(config, writer, verbose, compareResults)) {
            twtsWriter.write(twtId1, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition,
                             lowTapPosition, highTapPosition, twtTargetV, regulatedSide, twtV, connected, mainComponent, validated);
            twtsWriter.setValidationCompleted();
            if (compareResults) {
                twtsWriter.write(twtId2, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition,
                                 lowTapPosition, highTapPosition, twtTargetV, regulatedSide, twtV, connected, mainComponent, validated);
                twtsWriter.setValidationCompleted();
            }
            assertEquals(twtsContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getTwtsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults);

    @Test
    public void testTwtsMissingSide() throws Exception {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter twtsWriter = getTwtsValidationFormatterCsvWriter(config, writer, true, false)) {
            twtsWriter.write(twtId, Float.NaN, Float.NaN, Float.NaN, rho, rhoPreviousStep, rhoNextStep, tapPosition,
                             lowTapPosition, highTapPosition, twtTargetV, null, Float.NaN, false, false, true);
            assertEquals(getTwtsMissingSideContent(), writer.toString().trim());
        }
    }

    protected abstract String getTwtsMissingSideContent();

}
