/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.junit.Test;

import eu.itesla_project.commons.io.table.CsvTableFormatterFactory;
import eu.itesla_project.commons.io.table.TableFormatterConfig;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class ValidationFormatterCsvMultilineWriterTest {

    private final String branchId = "branchId";
    private final float p1 = 39.5056f;
    private final float p1Calc = 39.5058f;
    private final float q1 = -3.72344f;
    private final float q1Calc = -3.72348f;
    private final float p2 = -39.5122f;
    private final float p2Calc = -39.5128f;
    private final float q2 = 3.7746f;
    private final float q2Calc = 3.7742f;
    private final double r = 0.04;
    private final double x = 0.423;
    private final double g1 = 0.0;
    private final double g2 = 0.0;
    private final double b1 = 0.0;
    private final double b2 = 0.0;
    private final double rho1 = 1;
    private final double rho2 = 11.249999728;
    private final double alpha1 = 0.0;
    private final double alpha2 = 0.0;
    private final double u1 = 236.80258178710938;
    private final double u2 = 21.04814910888672;
    private final double theta1 = 0.1257718437996544;
    private final double theta2 = 0.12547118123496284;
    private final double z = Math.hypot(r, x);
    private final double y = 1 / z;
    private final double ksi = Math.atan2(r, x);

    private final String generatorId = "generatorId";
    private final float p = -39.5056f;
    private final float q = 3.72344f;
    private final float v = 380f;
    private final float targetP = 39.5056f;
    private final float targetQ = -3.72344f;
    private final float targetV = 380f;
    private final boolean connected = true;
    private final boolean voltageRegulatorOn = true;
    private final float minQ = -10f;
    private final float maxQ = 0f;

    @Test
    public void testFlows() throws Exception {
        String flowsContent = String.join(System.lineSeparator(),
                                          "test " + ValidationType.FLOWS + " check",
                                          String.join(";", "id", "characteristic", "value"),
                                          String.join(";", branchId, "network_p1", String.format(Locale.getDefault(), "%g", p1)),
                                          String.join(";", branchId, "expected_p1", String.format(Locale.getDefault(), "%g", p1Calc)),
                                          String.join(";", branchId, "network_q1", String.format(Locale.getDefault(), "%g", q1)),
                                          String.join(";", branchId, "expected_q1", String.format(Locale.getDefault(), "%g", q1Calc)),
                                          String.join(";", branchId, "network_p2", String.format(Locale.getDefault(), "%g", p2)),
                                          String.join(";", branchId, "expected_p2", String.format(Locale.getDefault(), "%g", p2Calc)),
                                          String.join(";", branchId, "network_q2", String.format(Locale.getDefault(), "%g", q2)),
                                          String.join(";", branchId, "expected_q2", String.format(Locale.getDefault(), "%g", q2Calc)));
        testFlows(flowsContent, false);
    }

    @Test
    public void testFlowsVerbose() throws Exception {
        String flowsContent = String.join(System.lineSeparator(),
                                          "test " + ValidationType.FLOWS + " check",
                                          String.join(";", "id", "characteristic", "value"),
                                          String.join(";", branchId, "network_p1", String.format(Locale.getDefault(), "%g", p1)),
                                          String.join(";", branchId, "expected_p1", String.format(Locale.getDefault(), "%g", p1Calc)),
                                          String.join(";", branchId, "network_q1", String.format(Locale.getDefault(), "%g", q1)),
                                          String.join(";", branchId, "expected_q1", String.format(Locale.getDefault(), "%g", q1Calc)),
                                          String.join(";", branchId, "network_p2", String.format(Locale.getDefault(), "%g", p2)),
                                          String.join(";", branchId, "expected_p2", String.format(Locale.getDefault(), "%g", p2Calc)),
                                          String.join(";", branchId, "network_q2", String.format(Locale.getDefault(), "%g", q2)),
                                          String.join(";", branchId, "expected_q2", String.format(Locale.getDefault(), "%g", q2Calc)),
                                          String.join(";", branchId, "r", String.format(Locale.getDefault(), "%g", r)),
                                          String.join(";", branchId, "x", String.format(Locale.getDefault(), "%g", x)),
                                          String.join(";", branchId, "g1", String.format(Locale.getDefault(), "%g", g1)),
                                          String.join(";", branchId, "g2", String.format(Locale.getDefault(), "%g", g2)),
                                          String.join(";", branchId, "b1", String.format(Locale.getDefault(), "%g", b1)),
                                          String.join(";", branchId, "b2", String.format(Locale.getDefault(), "%g", b2)),
                                          String.join(";", branchId, "rho1", String.format(Locale.getDefault(), "%g", rho1)),
                                          String.join(";", branchId, "rho2", String.format(Locale.getDefault(), "%g", rho2)),
                                          String.join(";", branchId, "alpha1", String.format(Locale.getDefault(), "%g", alpha1)),
                                          String.join(";", branchId, "alpha2", String.format(Locale.getDefault(), "%g", alpha2)),
                                          String.join(";", branchId, "u1", String.format(Locale.getDefault(), "%g", u1)),
                                          String.join(";", branchId, "u2", String.format(Locale.getDefault(), "%g", u2)),
                                          String.join(";", branchId, "theta1", String.format(Locale.getDefault(), "%g", theta1)),
                                          String.join(";", branchId, "theta2", String.format(Locale.getDefault(), "%g", theta2)),
                                          String.join(";", branchId, "z", String.format(Locale.getDefault(), "%g", z)),
                                          String.join(";", branchId, "y", String.format(Locale.getDefault(), "%g", y)),
                                          String.join(";", branchId, "ksi", String.format(Locale.getDefault(), "%g", ksi)));
        testFlows(flowsContent, true);
    }

    private void testFlows(String flowsContent, boolean verbose) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter flowsWriter = new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.FLOWS)) {
            flowsWriter.write(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2,
                              alpha1, alpha2, u1, u2, theta1, theta2, z, y, ksi);
            assertEquals(flowsContent, writer.toString().trim());
        }
    }

    @Test
    public void testGenerators() throws Exception {
        String generatorsContent = String.join(System.lineSeparator(),
                                               "test " + ValidationType.GENERATORS + " check",
                                               String.join(";", "id", "characteristic", "value"),
                                               String.join(";", generatorId, "p", String.format(Locale.getDefault(), "%g", p)),
                                               String.join(";", generatorId, "q", String.format(Locale.getDefault(), "%g", q)),
                                               String.join(";", generatorId, "v", String.format(Locale.getDefault(), "%g", v)),
                                               String.join(";", generatorId, "targetP", String.format(Locale.getDefault(), "%g", targetP)),
                                               String.join(";", generatorId, "targetQ", String.format(Locale.getDefault(), "%g", targetQ)),
                                               String.join(";", generatorId, "targetV", String.format(Locale.getDefault(), "%g", targetV)));
        testGenerators(generatorsContent, false);
    }

    @Test
    public void testGeneratorsVerbose() throws Exception {
        String generatorsContent = String.join(System.lineSeparator(),
                                               "test " + ValidationType.GENERATORS + " check",
                                               String.join(";", "id", "characteristic", "value"),
                                               String.join(";", generatorId, "p", String.format(Locale.getDefault(), "%g", p)),
                                               String.join(";", generatorId, "q", String.format(Locale.getDefault(), "%g", q)),
                                               String.join(";", generatorId, "v", String.format(Locale.getDefault(), "%g", v)),
                                               String.join(";", generatorId, "targetP", String.format(Locale.getDefault(), "%g", targetP)),
                                               String.join(";", generatorId, "targetQ", String.format(Locale.getDefault(), "%g", targetQ)),
                                               String.join(";", generatorId, "targetV", String.format(Locale.getDefault(), "%g", targetV)),
                                               String.join(";", generatorId, "connected", Boolean.toString(connected)),
                                               String.join(";", generatorId, "voltageRegulatorOn", Boolean.toString(voltageRegulatorOn)),
                                               String.join(";", generatorId, "minQ", String.format(Locale.getDefault(), "%g", minQ)),
                                               String.join(";", generatorId, "maxQ", String.format(Locale.getDefault(), "%g", maxQ)));
        testGenerators(generatorsContent, true);
    }

    private void testGenerators(String generatorsContent, boolean verbose) throws IOException {
        Writer writer = new StringWriter();
        TableFormatterConfig config = new TableFormatterConfig(Locale.getDefault(), ';', "inv", true, true);
        try (ValidationWriter generatorsWriter = new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.GENERATORS)) {
            generatorsWriter.write(generatorId, p, q, v, targetP, targetQ, targetV, connected, voltageRegulatorOn, minQ, maxQ);
            assertEquals(generatorsContent, writer.toString().trim());
        }
    }
}
