/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation.io;

import java.io.Writer;
import java.util.Locale;

import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.loadflow.validation.ValidationType;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class ValidationFormatterCsvWriterTest extends AbstractValidationFormatterWriterTest {

    @Override
    protected String getFlowsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.FLOWS + " check",
                           String.join(";", "id", "network_p1", "expected_p1", "network_q1", "expected_q1", "network_p2", "expected_p2",
                                       "network_q2", "expected_q2"),
                           String.join(";", branchId,
                                       String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1Calc),
                                       String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1Calc),
                                       String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2Calc),
                                       String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2Calc)));
    }

    @Override
    protected String getFlowsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.FLOWS + " check",
                           String.join(";", "id", "network_p1", "expected_p1", "network_q1", "expected_q1", "network_p2", "expected_p2",
                                       "network_q2", "expected_q2", "r", "x", "g1", "g2", "b1", "b2", "rho1", "rho2", "alpha1", "alpha2",
                                       "u1", "u2", "theta1", "theta2", "z", "y", "ksi", AbstractValidationFormatterWriter.CONNECTED + "1",
                                       AbstractValidationFormatterWriter.CONNECTED + "2", AbstractValidationFormatterWriter.MAIN_COMPONENT + "1",
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + "2", AbstractValidationFormatterWriter.VALIDATION),
                           String.join(";", branchId,
                                       String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1Calc),
                                       String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1Calc),
                                       String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2Calc),
                                       String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2Calc),
                                       String.format(Locale.getDefault(), "%g", r), String.format(Locale.getDefault(), "%g", x),
                                       String.format(Locale.getDefault(), "%g", g1), String.format(Locale.getDefault(), "%g", g2),
                                       String.format(Locale.getDefault(), "%g", b1), String.format(Locale.getDefault(), "%g", b2),
                                       String.format(Locale.getDefault(), "%g", rho1), String.format(Locale.getDefault(), "%g", rho2),
                                       String.format(Locale.getDefault(), "%g", alpha1), String.format(Locale.getDefault(), "%g", alpha2),
                                       String.format(Locale.getDefault(), "%g", u1), String.format(Locale.getDefault(), "%g", u2),
                                       String.format(Locale.getDefault(), "%g", theta1), String.format(Locale.getDefault(), "%g", theta2),
                                       String.format(Locale.getDefault(), "%g", z), String.format(Locale.getDefault(), "%g", y),
                                       String.format(Locale.getDefault(), "%g", ksi), Boolean.toString(connected1), Boolean.toString(connected2),
                                       Boolean.toString(mainComponent1), Boolean.toString(mainComponent2), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getFlowsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.FLOWS + " check",
                           String.join(";", "id", "network_p1", "expected_p1", "network_q1", "expected_q1", "network_p2", "expected_p2",
                                       "network_q2", "expected_q2",
                                       "network_p1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_p1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_q1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_q1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_p2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_p2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_q2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_q2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", branchId,
                                       String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1Calc),
                                       String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1Calc),
                                       String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2Calc),
                                       String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2Calc),
                                       String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1Calc),
                                       String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1Calc),
                                       String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2Calc),
                                       String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2Calc)));
    }

    @Override
    protected String getFlowsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.FLOWS + " check",
                           String.join(";", "id", "network_p1", "expected_p1", "network_q1", "expected_q1", "network_p2", "expected_p2",
                                       "network_q2", "expected_q2",
                                       "network_p1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_p1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_q1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_q1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_p2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_p2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_q2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_q2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherBranchId,
                                       "", "", "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1Calc),
                                       String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1Calc),
                                       String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2Calc),
                                       String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2Calc)),
                           String.join(";", branchId,
                                       String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1Calc),
                                       String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1Calc),
                                       String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2Calc),
                                       String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2Calc),
                                       "", "", "", "", "", "", "", ""));
    }

    @Override
    protected String getFlowsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.FLOWS + " check",
                           String.join(";", "id", "network_p1", "expected_p1", "network_q1", "expected_q1", "network_p2", "expected_p2",
                                       "network_q2", "expected_q2", "r", "x", "g1", "g2", "b1", "b2", "rho1", "rho2", "alpha1", "alpha2",
                                       "u1", "u2", "theta1", "theta2", "z", "y", "ksi", AbstractValidationFormatterWriter.CONNECTED + "1",
                                       AbstractValidationFormatterWriter.CONNECTED + "2", AbstractValidationFormatterWriter.MAIN_COMPONENT + "1",
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + "2", AbstractValidationFormatterWriter.VALIDATION,
                                       "network_p1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_p1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_q1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_q1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_p2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_p2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_q2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_q2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "r" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "x" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "g1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "g2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "b1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "b2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "rho1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "rho2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "alpha1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "alpha2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "u1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "u2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "theta1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "theta2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "z" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "y" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "ksi" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + "1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + "2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + "1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + "2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", branchId,
                                       String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1Calc),
                                       String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1Calc),
                                       String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2Calc),
                                       String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2Calc),
                                       String.format(Locale.getDefault(), "%g", r), String.format(Locale.getDefault(), "%g", x),
                                       String.format(Locale.getDefault(), "%g", g1), String.format(Locale.getDefault(), "%g", g2),
                                       String.format(Locale.getDefault(), "%g", b1), String.format(Locale.getDefault(), "%g", b2),
                                       String.format(Locale.getDefault(), "%g", rho1), String.format(Locale.getDefault(), "%g", rho2),
                                       String.format(Locale.getDefault(), "%g", alpha1), String.format(Locale.getDefault(), "%g", alpha2),
                                       String.format(Locale.getDefault(), "%g", u1), String.format(Locale.getDefault(), "%g", u2),
                                       String.format(Locale.getDefault(), "%g", theta1), String.format(Locale.getDefault(), "%g", theta2),
                                       String.format(Locale.getDefault(), "%g", z), String.format(Locale.getDefault(), "%g", y),
                                       String.format(Locale.getDefault(), "%g", ksi), Boolean.toString(connected1), Boolean.toString(connected2),
                                       Boolean.toString(mainComponent1), Boolean.toString(mainComponent2), AbstractValidationFormatterWriter.SUCCESS,
                                       String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1Calc),
                                       String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1Calc),
                                       String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2Calc),
                                       String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2Calc),
                                       String.format(Locale.getDefault(), "%g", r), String.format(Locale.getDefault(), "%g", x),
                                       String.format(Locale.getDefault(), "%g", g1), String.format(Locale.getDefault(), "%g", g2),
                                       String.format(Locale.getDefault(), "%g", b1), String.format(Locale.getDefault(), "%g", b2),
                                       String.format(Locale.getDefault(), "%g", rho1), String.format(Locale.getDefault(), "%g", rho2),
                                       String.format(Locale.getDefault(), "%g", alpha1), String.format(Locale.getDefault(), "%g", alpha2),
                                       String.format(Locale.getDefault(), "%g", u1), String.format(Locale.getDefault(), "%g", u2),
                                       String.format(Locale.getDefault(), "%g", theta1), String.format(Locale.getDefault(), "%g", theta2),
                                       String.format(Locale.getDefault(), "%g", z), String.format(Locale.getDefault(), "%g", y),
                                       String.format(Locale.getDefault(), "%g", ksi), Boolean.toString(connected1), Boolean.toString(connected2),
                                       Boolean.toString(mainComponent1), Boolean.toString(mainComponent2), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getFlowsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.FLOWS + " check",
                           String.join(";", "id", "network_p1", "expected_p1", "network_q1", "expected_q1", "network_p2", "expected_p2",
                                       "network_q2", "expected_q2", "r", "x", "g1", "g2", "b1", "b2", "rho1", "rho2", "alpha1", "alpha2",
                                       "u1", "u2", "theta1", "theta2", "z", "y", "ksi", AbstractValidationFormatterWriter.CONNECTED + "1",
                                       AbstractValidationFormatterWriter.CONNECTED + "2", AbstractValidationFormatterWriter.MAIN_COMPONENT + "1",
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + "2", AbstractValidationFormatterWriter.VALIDATION,
                                       "network_p1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_p1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_q1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_q1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_p2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_p2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "network_q2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expected_q2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "r" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "x" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "g1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "g2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "b1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "b2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "rho1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "rho2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "alpha1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "alpha2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "u1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "u2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "theta1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "theta2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "z" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "y" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "ksi" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + "1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + "2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + "1" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + "2" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherBranchId,
                                       "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1Calc),
                                       String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1Calc),
                                       String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2Calc),
                                       String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2Calc),
                                       String.format(Locale.getDefault(), "%g", r), String.format(Locale.getDefault(), "%g", x),
                                       String.format(Locale.getDefault(), "%g", g1), String.format(Locale.getDefault(), "%g", g2),
                                       String.format(Locale.getDefault(), "%g", b1), String.format(Locale.getDefault(), "%g", b2),
                                       String.format(Locale.getDefault(), "%g", rho1), String.format(Locale.getDefault(), "%g", rho2),
                                       String.format(Locale.getDefault(), "%g", alpha1), String.format(Locale.getDefault(), "%g", alpha2),
                                       String.format(Locale.getDefault(), "%g", u1), String.format(Locale.getDefault(), "%g", u2),
                                       String.format(Locale.getDefault(), "%g", theta1), String.format(Locale.getDefault(), "%g", theta2),
                                       String.format(Locale.getDefault(), "%g", z), String.format(Locale.getDefault(), "%g", y),
                                       String.format(Locale.getDefault(), "%g", ksi), Boolean.toString(connected1), Boolean.toString(connected2),
                                       Boolean.toString(mainComponent1), Boolean.toString(mainComponent2), AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", branchId,
                                       String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1Calc),
                                       String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1Calc),
                                       String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2Calc),
                                       String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2Calc),
                                       String.format(Locale.getDefault(), "%g", r), String.format(Locale.getDefault(), "%g", x),
                                       String.format(Locale.getDefault(), "%g", g1), String.format(Locale.getDefault(), "%g", g2),
                                       String.format(Locale.getDefault(), "%g", b1), String.format(Locale.getDefault(), "%g", b2),
                                       String.format(Locale.getDefault(), "%g", rho1), String.format(Locale.getDefault(), "%g", rho2),
                                       String.format(Locale.getDefault(), "%g", alpha1), String.format(Locale.getDefault(), "%g", alpha2),
                                       String.format(Locale.getDefault(), "%g", u1), String.format(Locale.getDefault(), "%g", u2),
                                       String.format(Locale.getDefault(), "%g", theta1), String.format(Locale.getDefault(), "%g", theta2),
                                       String.format(Locale.getDefault(), "%g", z), String.format(Locale.getDefault(), "%g", y),
                                       String.format(Locale.getDefault(), "%g", ksi), Boolean.toString(connected1), Boolean.toString(connected2),
                                       Boolean.toString(mainComponent1), Boolean.toString(mainComponent2), AbstractValidationFormatterWriter.SUCCESS,
                                       "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
    }

    @Override
    protected ValidationWriter getFlowsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.FLOWS, compareResults);
    }

    @Override
    protected String getGeneratorsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "p", "q", "v", "targetP", "targetQ", "targetV"),
                           String.join(";", generatorId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV)));
    }

    @Override
    protected String getGeneratorsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "p", "q", "v", "targetP", "targetQ", "targetV", AbstractValidationFormatterWriter.CONNECTED,
                                       "voltageRegulatorOn", "minP", "maxP", "minQ", "maxQ", AbstractValidationFormatterWriter.MAIN_COMPONENT,
                                       AbstractValidationFormatterWriter.VALIDATION),
                           String.join(";", generatorId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minP), String.format(Locale.getDefault(), "%g", maxP),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getGeneratorsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "p", "q", "v", "targetP", "targetQ", "targetV",
                                       "p" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetV" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", generatorId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV)));
    }

    @Override
    protected String getGeneratorsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "p", "q", "v", "targetP", "targetQ", "targetV",
                                       "p" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetV" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherGeneratorId,
                                       "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV)),
                           String.join(";", generatorId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       "", "", "", "", "", ""));
    }

    @Override
    protected String getGeneratorsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "p", "q", "v", "targetP", "targetQ", "targetV", AbstractValidationFormatterWriter.CONNECTED,
                                       "voltageRegulatorOn", "minP", "maxP", "minQ", "maxQ", AbstractValidationFormatterWriter.MAIN_COMPONENT,
                                       AbstractValidationFormatterWriter.VALIDATION,
                                       "p" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetV" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "voltageRegulatorOn" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "minP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "maxP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "minQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "maxQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT +  AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", generatorId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minP), String.format(Locale.getDefault(), "%g", maxP),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minP), String.format(Locale.getDefault(), "%g", maxP),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getGeneratorsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "p", "q", "v", "targetP", "targetQ", "targetV", AbstractValidationFormatterWriter.CONNECTED,
                                       "voltageRegulatorOn", "minP", "maxP", "minQ", "maxQ", AbstractValidationFormatterWriter.MAIN_COMPONENT,
                                       AbstractValidationFormatterWriter.VALIDATION,
                                       "p" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "targetV" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "voltageRegulatorOn" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "minP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "maxP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "minQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "maxQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT +  AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherGeneratorId,
                                       "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minP), String.format(Locale.getDefault(), "%g", maxP),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", generatorId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minP), String.format(Locale.getDefault(), "%g", maxP),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS,
                                       "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
    }

    @Override
    protected ValidationWriter getGeneratorsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.GENERATORS, compareResults);
    }

    @Override
    protected String getBusesContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "incomingP", "incomingQ", "loadP", "loadQ"),
                           String.join(";", busId,
                                       String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                                       String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ)));
    }

    @Override
    protected String getBusesVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "incomingP", "incomingQ", "loadP", "loadQ", "genP", "genQ", "shuntP", "shuntQ", "svcP", "svcQ",
                                       "vscCSP", "vscCSQ", "lineP", "lineQ", "danglingLineP", "danglingLineQ", "twtP", "twtQ", "tltP", "tltQ",
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT, AbstractValidationFormatterWriter.VALIDATION),
                           String.join(";", busId,
                                       String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                                       String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ),
                                       String.format(Locale.getDefault(), "%g", genP), String.format(Locale.getDefault(), "%g", genQ),
                                       String.format(Locale.getDefault(), "%g", shuntP), String.format(Locale.getDefault(), "%g", shuntQ),
                                       String.format(Locale.getDefault(), "%g", svcP), String.format(Locale.getDefault(), "%g", svcQ),
                                       String.format(Locale.getDefault(), "%g", vscCSP), String.format(Locale.getDefault(), "%g", vscCSQ),
                                       String.format(Locale.getDefault(), "%g", lineP), String.format(Locale.getDefault(), "%g", lineQ),
                                       String.format(Locale.getDefault(), "%g", danglingLineP), String.format(Locale.getDefault(), "%g", danglingLineQ),
                                       String.format(Locale.getDefault(), "%g", twtP), String.format(Locale.getDefault(), "%g", twtQ),
                                       String.format(Locale.getDefault(), "%g", tltP), String.format(Locale.getDefault(), "%g", tltQ),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getBusesCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "incomingP", "incomingQ", "loadP", "loadQ",
                                       "incomingP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "incomingQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "loadP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "loadQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", busId,
                                       String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                                       String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ),
                                       String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                                       String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ)));
    }

    @Override
    protected String getBusesCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "incomingP", "incomingQ", "loadP", "loadQ",
                                       "incomingP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "incomingQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "loadP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "loadQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherBusId,
                                       "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                                       String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ)),
                           String.join(";", busId,
                                       String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                                       String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ),
                                       "", "", "", ""));
    }

    @Override
    protected String getBusesCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "incomingP", "incomingQ", "loadP", "loadQ", "genP", "genQ", "shuntP", "shuntQ", "svcP", "svcQ",
                                       "vscCSP", "vscCSQ", "lineP", "lineQ", "danglingLineP", "danglingLineQ", "twtP", "twtQ", "tltP", "tltQ",
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT, AbstractValidationFormatterWriter.VALIDATION,
                                       "incomingP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "incomingQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "loadP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "loadQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "genP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "genQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "shuntP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "shuntQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "svcP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "svcQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "vscCSP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "vscCSQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "lineP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "lineQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "danglingLineP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "danglingLineQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "twtP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "twtQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "tltP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "tltQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", busId,
                                       String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                                       String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ),
                                       String.format(Locale.getDefault(), "%g", genP), String.format(Locale.getDefault(), "%g", genQ),
                                       String.format(Locale.getDefault(), "%g", shuntP), String.format(Locale.getDefault(), "%g", shuntQ),
                                       String.format(Locale.getDefault(), "%g", svcP), String.format(Locale.getDefault(), "%g", svcQ),
                                       String.format(Locale.getDefault(), "%g", vscCSP), String.format(Locale.getDefault(), "%g", vscCSQ),
                                       String.format(Locale.getDefault(), "%g", lineP), String.format(Locale.getDefault(), "%g", lineQ),
                                       String.format(Locale.getDefault(), "%g", danglingLineP), String.format(Locale.getDefault(), "%g", danglingLineQ),
                                       String.format(Locale.getDefault(), "%g", twtP), String.format(Locale.getDefault(), "%g", twtQ),
                                       String.format(Locale.getDefault(), "%g", tltP), String.format(Locale.getDefault(), "%g", tltQ),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS,
                                       String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                                       String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ),
                                       String.format(Locale.getDefault(), "%g", genP), String.format(Locale.getDefault(), "%g", genQ),
                                       String.format(Locale.getDefault(), "%g", shuntP), String.format(Locale.getDefault(), "%g", shuntQ),
                                       String.format(Locale.getDefault(), "%g", svcP), String.format(Locale.getDefault(), "%g", svcQ),
                                       String.format(Locale.getDefault(), "%g", vscCSP), String.format(Locale.getDefault(), "%g", vscCSQ),
                                       String.format(Locale.getDefault(), "%g", lineP), String.format(Locale.getDefault(), "%g", lineQ),
                                       String.format(Locale.getDefault(), "%g", danglingLineP), String.format(Locale.getDefault(), "%g", danglingLineQ),
                                       String.format(Locale.getDefault(), "%g", twtP), String.format(Locale.getDefault(), "%g", twtQ),
                                       String.format(Locale.getDefault(), "%g", tltP), String.format(Locale.getDefault(), "%g", tltQ),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getBusesCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "incomingP", "incomingQ", "loadP", "loadQ", "genP", "genQ", "shuntP", "shuntQ", "svcP", "svcQ",
                                       "vscCSP", "vscCSQ", "lineP", "lineQ", "danglingLineP", "danglingLineQ", "twtP", "twtQ", "tltP", "tltQ",
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT, AbstractValidationFormatterWriter.VALIDATION,
                                       "incomingP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "incomingQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "loadP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "loadQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "genP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "genQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "shuntP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "shuntQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "svcP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "svcQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "vscCSP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "vscCSQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "lineP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "lineQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "danglingLineP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "danglingLineQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "twtP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "twtQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "tltP" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX, "tltQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherBusId,
                                       "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                                       String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ),
                                       String.format(Locale.getDefault(), "%g", genP), String.format(Locale.getDefault(), "%g", genQ),
                                       String.format(Locale.getDefault(), "%g", shuntP), String.format(Locale.getDefault(), "%g", shuntQ),
                                       String.format(Locale.getDefault(), "%g", svcP), String.format(Locale.getDefault(), "%g", svcQ),
                                       String.format(Locale.getDefault(), "%g", vscCSP), String.format(Locale.getDefault(), "%g", vscCSQ),
                                       String.format(Locale.getDefault(), "%g", lineP), String.format(Locale.getDefault(), "%g", lineQ),
                                       String.format(Locale.getDefault(), "%g", danglingLineP), String.format(Locale.getDefault(), "%g", danglingLineQ),
                                       String.format(Locale.getDefault(), "%g", twtP), String.format(Locale.getDefault(), "%g", twtQ),
                                       String.format(Locale.getDefault(), "%g", tltP), String.format(Locale.getDefault(), "%g", tltQ),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", busId,
                                       String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                                       String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ),
                                       String.format(Locale.getDefault(), "%g", genP), String.format(Locale.getDefault(), "%g", genQ),
                                       String.format(Locale.getDefault(), "%g", shuntP), String.format(Locale.getDefault(), "%g", shuntQ),
                                       String.format(Locale.getDefault(), "%g", svcP), String.format(Locale.getDefault(), "%g", svcQ),
                                       String.format(Locale.getDefault(), "%g", vscCSP), String.format(Locale.getDefault(), "%g", vscCSQ),
                                       String.format(Locale.getDefault(), "%g", lineP), String.format(Locale.getDefault(), "%g", lineQ),
                                       String.format(Locale.getDefault(), "%g", danglingLineP), String.format(Locale.getDefault(), "%g", danglingLineQ),
                                       String.format(Locale.getDefault(), "%g", twtP), String.format(Locale.getDefault(), "%g", twtQ),
                                       String.format(Locale.getDefault(), "%g", tltP), String.format(Locale.getDefault(), "%g", tltQ),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS,
                                       "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
    }

    @Override
    protected ValidationWriter getBusesValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.BUSES, compareResults);
    }

    @Override
    protected String getSvcsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", AbstractValidationFormatterWriter.NOMINAL_V, "reactivePowerSetpoint", "voltageSetpoint"),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", nominalV),
                                       String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint)));
    }

    @Override
    protected String getSvcsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", AbstractValidationFormatterWriter.NOMINAL_V, "reactivePowerSetpoint", "voltageSetpoint",
                                       AbstractValidationFormatterWriter.CONNECTED, "regulationMode", "bMin", "bMax", AbstractValidationFormatterWriter.MAIN_COMPONENT,
                                       AbstractValidationFormatterWriter.VALIDATION),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", nominalV),
                                       String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), Boolean.toString(mainComponent),
                                       AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getSvcsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", AbstractValidationFormatterWriter.NOMINAL_V, "reactivePowerSetpoint", "voltageSetpoint",
                                       "p" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.NOMINAL_V + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "reactivePowerSetpoint" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "voltageSetpoint" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", nominalV),
                                       String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint),
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", nominalV),
                                       String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint)));
    }

    @Override
    protected String getSvcsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", AbstractValidationFormatterWriter.NOMINAL_V, "reactivePowerSetpoint", "voltageSetpoint",
                                       "p" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.NOMINAL_V + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "reactivePowerSetpoint" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "voltageSetpoint" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherSvcId,
                                       "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", nominalV),
                                       String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint)),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", nominalV),
                                       String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint),
                                       "", "", "", "", "", ""));
    }

    @Override
    protected String getSvcsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", AbstractValidationFormatterWriter.NOMINAL_V, "reactivePowerSetpoint", "voltageSetpoint",
                                       AbstractValidationFormatterWriter.CONNECTED, "regulationMode", "bMin", "bMax", AbstractValidationFormatterWriter.MAIN_COMPONENT,
                                       AbstractValidationFormatterWriter.VALIDATION,
                                       "p" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.NOMINAL_V + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "reactivePowerSetpoint" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "voltageSetpoint" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "regulationMode" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "bMin" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "bMax" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT +  AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", nominalV),
                                       String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), Boolean.toString(mainComponent),
                                       AbstractValidationFormatterWriter.SUCCESS,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", nominalV),
                                       String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), Boolean.toString(mainComponent),
                                       AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getSvcsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", AbstractValidationFormatterWriter.NOMINAL_V, "reactivePowerSetpoint", "voltageSetpoint",
                                       AbstractValidationFormatterWriter.CONNECTED, "regulationMode", "bMin", "bMax", AbstractValidationFormatterWriter.MAIN_COMPONENT,
                                       AbstractValidationFormatterWriter.VALIDATION,
                                       "p" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.NOMINAL_V + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "reactivePowerSetpoint" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "voltageSetpoint" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "regulationMode" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "bMin" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "bMax" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT +  AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherSvcId,
                                       "", "", "", "", "", "", "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", nominalV),
                                       String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), Boolean.toString(mainComponent),
                                       AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", nominalV),
                                       String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), Boolean.toString(mainComponent),
                                       AbstractValidationFormatterWriter.SUCCESS,
                                       "", "", "", "", "", "", "", "", "", "", "", ""));
    }

    @Override
    protected ValidationWriter getSvcsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.SVCS, compareResults);
    }

    @Override
    protected String getShuntsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "q", "expectedQ"),
                           String.join(";", shuntId,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ)));
    }

    @Override
    protected String getShuntsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "q", "expectedQ", "p", "currentSectionCount", "maximumSectionCount", "bPerSection", "v",
                                       AbstractValidationFormatterWriter.CONNECTED, "qMax", AbstractValidationFormatterWriter.NOMINAL_V,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT, AbstractValidationFormatterWriter.VALIDATION),
                           String.join(";", shuntId,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", p), Integer.toString(currentSectionCount),
                                       Integer.toString(maximumSectionCount), String.format(Locale.getDefault(), "%g", bPerSection),
                                       String.format(Locale.getDefault(), "%g", v), Boolean.toString(connected),
                                       String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", nominalV),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getShuntsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "q", "expectedQ",
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expectedQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", shuntId,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ)));
    }

    @Override
    protected String getShuntsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "q", "expectedQ",
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expectedQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherShuntId,
                                       "", "", String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ)),
                           String.join(";", shuntId,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ), "", ""));
    }

    @Override
    protected String getShuntsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "q", "expectedQ", "p", "currentSectionCount", "maximumSectionCount", "bPerSection", "v",
                                       AbstractValidationFormatterWriter.CONNECTED, "qMax", AbstractValidationFormatterWriter.NOMINAL_V,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT, AbstractValidationFormatterWriter.VALIDATION,
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expectedQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "p" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "currentSectionCount" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "maximumSectionCount" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "bPerSection" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "qMax" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.NOMINAL_V + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", shuntId,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", p), Integer.toString(currentSectionCount),
                                       Integer.toString(maximumSectionCount), String.format(Locale.getDefault(), "%g", bPerSection),
                                       String.format(Locale.getDefault(), "%g", v), Boolean.toString(connected),
                                       String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", nominalV),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", p), Integer.toString(currentSectionCount),
                                       Integer.toString(maximumSectionCount), String.format(Locale.getDefault(), "%g", bPerSection),
                                       String.format(Locale.getDefault(), "%g", v), Boolean.toString(connected),
                                       String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", nominalV),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getShuntsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "q", "expectedQ", "p", "currentSectionCount", "maximumSectionCount", "bPerSection", "v",
                                       AbstractValidationFormatterWriter.CONNECTED, "qMax", AbstractValidationFormatterWriter.NOMINAL_V,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT, AbstractValidationFormatterWriter.VALIDATION,
                                       "q" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "expectedQ" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "p" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "currentSectionCount" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "maximumSectionCount" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "bPerSection" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "qMax" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.NOMINAL_V + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.MAIN_COMPONENT + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherShuntId,
                                       "", "", "", "", "", "", "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", p), Integer.toString(currentSectionCount),
                                       Integer.toString(maximumSectionCount), String.format(Locale.getDefault(), "%g", bPerSection),
                                       String.format(Locale.getDefault(), "%g", v), Boolean.toString(connected),
                                       String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", nominalV),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", shuntId,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", p), Integer.toString(currentSectionCount),
                                       Integer.toString(maximumSectionCount), String.format(Locale.getDefault(), "%g", bPerSection),
                                       String.format(Locale.getDefault(), "%g", v), Boolean.toString(connected),
                                       String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", nominalV),
                                       Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS,
                                       "", "", "", "", "", "", "", "", "", "", "", ""));
    }

    @Override
    protected ValidationWriter getShuntsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.SHUNTS, compareResults);
    }

    @Override
    protected String getTwtsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.TWTS + " check",
                           String.join(";", "id", "error", "upIncrement", "downIncrement"),
                           String.join(";", twtId,
                                       String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", upIncrement),
                                       String.format(Locale.getDefault(), "%g", downIncrement)));
    }

    @Override
    protected String getTwtsVerboseContent() {
        return String.join(System.lineSeparator(),
                "test " + ValidationType.TWTS + " check",
                String.join(";", "id", "error", "upIncrement", "downIncrement", "rho", "rhoPreviousStep", "rhoNextStep", "tapPosition", "lowTapPosition",
                            "highTapPosition", "tapChangerTargetV", "regulatedSide", "v", AbstractValidationFormatterWriter.CONNECTED,
                            AbstractValidationFormatterWriter.MAIN_COMPONENT, AbstractValidationFormatterWriter.VALIDATION),
                String.join(";", twtId,
                            String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", upIncrement),
                            String.format(Locale.getDefault(), "%g", downIncrement), String.format(Locale.getDefault(), "%g", rho),
                            String.format(Locale.getDefault(), "%g", rhoPreviousStep), String.format(Locale.getDefault(), "%g", rhoNextStep),
                            Integer.toString(tapPosition), Integer.toString(lowTapPosition), Integer.toString(highTapPosition),
                            String.format(Locale.getDefault(), "%g", twtTargetV), regulatedSide.name(), String.format(Locale.getDefault(), "%g", twtV),
                            Boolean.toString(connected), Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getTwtsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.TWTS + " check",
                           String.join(";", "id", "error", "upIncrement", "downIncrement",
                                       "error" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "upIncrement" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "downIncrement" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", twtId,
                                       String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", upIncrement),
                                       String.format(Locale.getDefault(), "%g", downIncrement),
                                       String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", upIncrement),
                                       String.format(Locale.getDefault(), "%g", downIncrement)));
    }

    @Override
    protected String getTwtsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.TWTS + " check",
                           String.join(";", "id", "error", "upIncrement", "downIncrement",
                                       "error" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "upIncrement" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                                       "downIncrement" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherTwtId,
                                       "", "", "",
                                       String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", upIncrement),
                                       String.format(Locale.getDefault(), "%g", downIncrement)),
                           String.join(";", twtId,
                                       String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", upIncrement),
                                       String.format(Locale.getDefault(), "%g", downIncrement),
                                       "", "", ""));
    }

    @Override
    protected String getTwtsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                "test " + ValidationType.TWTS + " check",
                String.join(";", "id", "error", "upIncrement", "downIncrement", "rho", "rhoPreviousStep", "rhoNextStep", "tapPosition", "lowTapPosition",
                            "highTapPosition", "tapChangerTargetV", "regulatedSide", "v", AbstractValidationFormatterWriter.CONNECTED,
                            AbstractValidationFormatterWriter.MAIN_COMPONENT, AbstractValidationFormatterWriter.VALIDATION,
                            "error" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "upIncrement" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "downIncrement" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "rho" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "rhoPreviousStep" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "rhoNextStep" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "tapPosition" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "lowTapPosition" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "highTapPosition" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "tapChangerTargetV" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "regulatedSide" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            AbstractValidationFormatterWriter.MAIN_COMPONENT + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                String.join(";", twtId,
                            String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", upIncrement),
                            String.format(Locale.getDefault(), "%g", downIncrement), String.format(Locale.getDefault(), "%g", rho),
                            String.format(Locale.getDefault(), "%g", rhoPreviousStep), String.format(Locale.getDefault(), "%g", rhoNextStep),
                            Integer.toString(tapPosition), Integer.toString(lowTapPosition), Integer.toString(highTapPosition),
                            String.format(Locale.getDefault(), "%g", twtTargetV), regulatedSide.name(), String.format(Locale.getDefault(), "%g", twtV),
                            Boolean.toString(connected), Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS,
                            String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", upIncrement),
                            String.format(Locale.getDefault(), "%g", downIncrement), String.format(Locale.getDefault(), "%g", rho),
                            String.format(Locale.getDefault(), "%g", rhoPreviousStep), String.format(Locale.getDefault(), "%g", rhoNextStep),
                            Integer.toString(tapPosition), Integer.toString(lowTapPosition), Integer.toString(highTapPosition),
                            String.format(Locale.getDefault(), "%g", twtTargetV), regulatedSide.name(), String.format(Locale.getDefault(), "%g", twtV),
                            Boolean.toString(connected), Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getTwtsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                "test " + ValidationType.TWTS + " check",
                String.join(";", "id", "error", "upIncrement", "downIncrement", "rho", "rhoPreviousStep", "rhoNextStep", "tapPosition", "lowTapPosition",
                            "highTapPosition", "tapChangerTargetV", "regulatedSide", "v", AbstractValidationFormatterWriter.CONNECTED,
                            AbstractValidationFormatterWriter.MAIN_COMPONENT, AbstractValidationFormatterWriter.VALIDATION,
                            "error" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "upIncrement" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "downIncrement" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "rho" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "rhoPreviousStep" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "rhoNextStep" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "tapPosition" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "lowTapPosition" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "highTapPosition" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "tapChangerTargetV" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "regulatedSide" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            "v" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            AbstractValidationFormatterWriter.MAIN_COMPONENT + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX,
                            AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                String.join(";", otherTwtId,
                            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                            String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", upIncrement),
                            String.format(Locale.getDefault(), "%g", downIncrement), String.format(Locale.getDefault(), "%g", rho),
                            String.format(Locale.getDefault(), "%g", rhoPreviousStep), String.format(Locale.getDefault(), "%g", rhoNextStep),
                            Integer.toString(tapPosition), Integer.toString(lowTapPosition), Integer.toString(highTapPosition),
                            String.format(Locale.getDefault(), "%g", twtTargetV), regulatedSide.name(), String.format(Locale.getDefault(), "%g", twtV),
                            Boolean.toString(connected), Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS),
                String.join(";", twtId,
                            String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", upIncrement),
                            String.format(Locale.getDefault(), "%g", downIncrement), String.format(Locale.getDefault(), "%g", rho),
                            String.format(Locale.getDefault(), "%g", rhoPreviousStep), String.format(Locale.getDefault(), "%g", rhoNextStep),
                            Integer.toString(tapPosition), Integer.toString(lowTapPosition), Integer.toString(highTapPosition),
                            String.format(Locale.getDefault(), "%g", twtTargetV), regulatedSide.name(), String.format(Locale.getDefault(), "%g", twtV),
                            Boolean.toString(connected), Boolean.toString(mainComponent), AbstractValidationFormatterWriter.SUCCESS,
                            "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
    }

    @Override
    protected ValidationWriter getTwtsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.TWTS, compareResults);
    }

    @Override
    protected String getTwtsMissingSideContent() {
        return String.join(System.lineSeparator(),
                "test " + ValidationType.TWTS + " check",
                String.join(";", "id", "error", "upIncrement", "downIncrement", "rho", "rhoPreviousStep", "rhoNextStep", "tapPosition", "lowTapPosition",
                            "highTapPosition", "tapChangerTargetV", "regulatedSide", "v", AbstractValidationFormatterWriter.CONNECTED,
                            AbstractValidationFormatterWriter.MAIN_COMPONENT, AbstractValidationFormatterWriter.VALIDATION),
                String.join(";", twtId,
                            "inv", "inv", "inv", String.format(Locale.getDefault(), "%g", rho),
                            String.format(Locale.getDefault(), "%g", rhoPreviousStep), String.format(Locale.getDefault(), "%g", rhoNextStep),
                            Integer.toString(tapPosition), Integer.toString(lowTapPosition), Integer.toString(highTapPosition),
                            String.format(Locale.getDefault(), "%g", twtTargetV), "inv", "inv",
                            Boolean.toString(false), Boolean.toString(false), AbstractValidationFormatterWriter.SUCCESS));
    }

}
