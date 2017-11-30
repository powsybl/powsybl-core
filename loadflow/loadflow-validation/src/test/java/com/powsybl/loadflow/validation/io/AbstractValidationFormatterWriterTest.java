/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractValidationFormatterWriterTest {

    protected final String branchId = "branchId";
    protected final float p1 = 39.5056f;
    protected final float p1Calc = 39.5058f;
    protected final float q1 = -3.72344f;
    protected final float q1Calc = -3.72348f;
    protected final float p2 = -39.5122f;
    protected final float p2Calc = -39.5128f;
    protected final float q2 = 3.7746f;
    protected final float q2Calc = 3.7742f;
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
    protected final boolean validated = true;

    protected final String generatorId = "generatorId";
    protected final float p = -39.5056f;
    protected final float q = 3.72344f;
    protected final float v = 380f;
    protected final float targetP = 39.5056f;
    protected final float targetQ = -3.72344f;
    protected final float targetV = 380f;
    protected final boolean connected = true;
    protected final boolean voltageRegulatorOn = true;
    protected final float minQ = -10f;
    protected final float maxQ = 0f;

    protected final String busId = "busId";
    protected final double incomingP = -37.2287f;
    protected final double incomingQ = -174.383f;
    protected final double loadP = 37.2286f;
    protected final double loadQ = 174.38244f;
    protected final double genP = -2020f;
    protected final double genQ = 91.54;
    protected final double shuntP = 0f;
    protected final double shuntQ = 175.8437f;
    protected final double svcP = 0f;
    protected final double svcQ = 0f;
    protected final double vscCSP = 0f;
    protected final double vscCSQ = 0f;
    protected final double lineP = 1982.7713f;
    protected final double lineQ = -441.7662f;
    protected final double twtP = 0f;
    protected final double twtQ = 0f;
    protected final double tltP = 0f;
    protected final double tltQ = 0f;

    protected final String svcId = "svcId";
    protected final float reactivePowerSetpoint = -3.72344f;
    protected final float voltageSetpoint = 380f;
    protected final RegulationMode regulationMode = RegulationMode.VOLTAGE;
    protected final float bMin = -10f;
    protected final float bMax = 0f;

    protected final String shuntId = "shuntId";
    protected final float expectedQ = 3.724f;
    protected int currentSectionCount = 0;
    protected int maximumSectionCount = 1;
    protected float bPerSection = -0.16f;
    protected float qMax = -144.4f;
    protected final float nominalV = 380f;

    @Test
    public void testFlows() throws Exception {
        testFlows(getFlowsContent(), false);
    }

    protected abstract String getFlowsContent();

    @Test
    public void testFlowsVerbose() throws Exception {
        testFlows(getFlowsVerboseContent(), true);
    }

    protected abstract String getFlowsVerboseContent();

    protected void testFlows(String flowsContent, boolean verbose) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter flowsWriter = getFlowsValidationFormatterCsvWriter(config, writer, verbose)) {
            flowsWriter.write(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2,
                              alpha1, alpha2, u1, u2, theta1, theta2, z, y, ksi, validated);
            assertEquals(flowsContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getFlowsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose);

    @Test
    public void testGenerators() throws Exception {
        testGenerators(getGeneratorsContent(), false);
    }

    protected abstract String getGeneratorsContent();

    @Test
    public void testGeneratorsVerbose() throws Exception {
        testGenerators(getGeneratorsVerboseContent(), true);
    }

    protected abstract String getGeneratorsVerboseContent();

    protected void testGenerators(String generatorsContent, boolean verbose) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter generatorsWriter = getGeneratorsValidationFormatterCsvWriter(config, writer, verbose)) {
            generatorsWriter.write(generatorId, p, q, v, targetP, targetQ, targetV, connected, voltageRegulatorOn, minQ, maxQ, validated);
            assertEquals(generatorsContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getGeneratorsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose);

    @Test
    public void testBuses() throws Exception {
        testBuses(getBusesContent(), false);
    }

    protected abstract String getBusesContent();

    @Test
    public void testBusesVerbose() throws Exception {
        testBuses(getBusesVerboseContent(), true);
    }

    protected abstract String getBusesVerboseContent();

    private void testBuses(String busesContent, boolean verbose) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter busesWriter = getBusesValidationFormatterCsvWriter(config, writer, verbose)) {
            busesWriter.write(busId, incomingP, incomingQ, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, twtP, twtQ, tltP, tltQ, validated);
            assertEquals(busesContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getBusesValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose);

    @Test
    public void testSvcs() throws Exception {
        testSvcs(getSvcsContent(), false);
    }

    protected abstract String getSvcsContent();

    @Test
    public void testSvcsVerbose() throws Exception {
        testSvcs(getSvcsVerboseContent(), true);
    }

    protected abstract String getSvcsVerboseContent();

    protected void testSvcs(String svcsContent, boolean verbose) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter svcsWriter = getSvcsValidationFormatterCsvWriter(config, writer, verbose)) {
            svcsWriter.write(svcId, p, q, v, reactivePowerSetpoint, voltageSetpoint, verbose, regulationMode, bMin, bMax, validated);
            assertEquals(svcsContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getSvcsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose);

    @Test
    public void testShunts() throws Exception {
        testShunts(getShuntsContent(), false);
    }

    protected abstract String getShuntsContent();

    @Test
    public void testShuntsVerbose() throws Exception {
        testShunts(getShuntsVerboseContent(), true);
    }

    protected abstract String getShuntsVerboseContent();

    protected void testShunts(String shuntsContent, boolean verbose) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter shuntsWriter = getShuntsValidationFormatterCsvWriter(config, writer, verbose)) {
            shuntsWriter.write(shuntId, q, expectedQ, p, currentSectionCount, maximumSectionCount, bPerSection, v, connected, qMax, nominalV, validated);
            assertEquals(shuntsContent, writer.toString().trim());
        }
    }

    protected abstract ValidationWriter getShuntsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose);

}
