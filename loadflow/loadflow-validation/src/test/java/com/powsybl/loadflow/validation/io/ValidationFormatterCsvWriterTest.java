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
                                       "u1", "u2", "theta1", "theta2", "z", "y", "ksi", "connected1", "connected2", "mainComponent1",
                                       "mainComponent2", AbstractValidationFormatterWriter.VALIDATION),
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
                                       "network_p1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_p1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_q1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_q1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_p2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_p2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_q2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_q2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
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
                                       "network_p1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_p1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_q1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_q1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_p2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_p2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_q2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_q2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
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
                                       "u1", "u2", "theta1", "theta2", "z", "y", "ksi", "connected1", "connected2", "mainComponent1",
                                       "mainComponent2", AbstractValidationFormatterWriter.VALIDATION,
                                       "network_p1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_p1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_q1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_q1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_p2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_p2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_q2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_q2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "r" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "x" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "g1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "g2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "b1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "b2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "rho1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "rho2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "alpha1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "alpha2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "u1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "u2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "theta1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "theta2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "z" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "y" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "ksi" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "connected1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "connected2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "mainComponent1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "mainComponent2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
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
                                       "u1", "u2", "theta1", "theta2", "z", "y", "ksi", "connected1", "connected2", "mainComponent1",
                                       "mainComponent2", AbstractValidationFormatterWriter.VALIDATION,
                                       "network_p1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_p1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_q1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_q1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_p2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_p2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "network_q2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expected_q2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "r" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "x" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "g1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "g2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "b1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "b2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "rho1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "rho2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "alpha1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "alpha2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "u1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "u2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "theta1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "theta2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "z" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "y" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "ksi" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "connected1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "connected2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "mainComponent1" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "mainComponent2" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
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
                                       "voltageRegulatorOn", "minQ", "maxQ", AbstractValidationFormatterWriter.VALIDATION),
                           String.join(";", generatorId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ),
                                       AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getGeneratorsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "p", "q", "v", "targetP", "targetQ", "targetV",
                                       "p" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetV" + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
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
                                       "p" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetV" + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
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
                                       "voltageRegulatorOn", "minQ", "maxQ", AbstractValidationFormatterWriter.VALIDATION,
                                       "p" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetV" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "voltageRegulatorOn" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "minQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "maxQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
                           String.join(";", generatorId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ),
                                       AbstractValidationFormatterWriter.SUCCESS,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ),
                                       AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getGeneratorsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "p", "q", "v", "targetP", "targetQ", "targetV", AbstractValidationFormatterWriter.CONNECTED,
                                       "voltageRegulatorOn", "minQ", "maxQ", AbstractValidationFormatterWriter.VALIDATION,
                                       "p" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "targetV" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "voltageRegulatorOn" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "minQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "maxQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
                           String.join(";", otherGeneratorId,
                                       "", "", "", "", "", "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ),
                                       AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", generatorId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ),
                                       AbstractValidationFormatterWriter.SUCCESS,
                                       "", "", "", "", "", "", "", "", "", "", ""));
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
                                       "vscCSP", "vscCSQ", "lineP", "lineQ", "danglingLineP", "danglingLineQ", "twtP", "twtQ", "tltP", "tltQ", AbstractValidationFormatterWriter.VALIDATION),
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
                                       AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getBusesCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "incomingP", "incomingQ", "loadP", "loadQ",
                                       "incomingP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "incomingQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "loadP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "loadQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
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
                                       "incomingP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "incomingQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "loadP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "loadQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
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
                                       "vscCSP", "vscCSQ", "lineP", "lineQ", "danglingLineP", "danglingLineQ", "twtP", "twtQ", "tltP", "tltQ", AbstractValidationFormatterWriter.VALIDATION,
                                       "incomingP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "incomingQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "loadP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "loadQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "genP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "genQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "shuntP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "shuntQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "svcP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "svcQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "vscCSP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "vscCSQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "lineP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "lineQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "danglingLineP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "danglingLineQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "twtP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "twtQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "tltP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "tltQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
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
                                       AbstractValidationFormatterWriter.SUCCESS,
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
                                       AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getBusesCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "incomingP", "incomingQ", "loadP", "loadQ", "genP", "genQ", "shuntP", "shuntQ", "svcP", "svcQ",
                                       "vscCSP", "vscCSQ", "lineP", "lineQ", "danglingLineP", "danglingLineQ", "twtP", "twtQ", "tltP", "tltQ", AbstractValidationFormatterWriter.VALIDATION,
                                       "incomingP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "incomingQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "loadP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "loadQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "genP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "genQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "shuntP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "shuntQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "svcP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "svcQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "vscCSP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "vscCSQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "lineP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "lineQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "danglingLineP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "danglingLineQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "twtP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "twtQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "tltP" + AbstractValidationFormatterWriter.POST_LF_SUFFIX, "tltQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
                           String.join(";", otherBusId,
                                       "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
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
                                       AbstractValidationFormatterWriter.SUCCESS),
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
                                       AbstractValidationFormatterWriter.SUCCESS,
                                       "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
    }

    @Override
    protected ValidationWriter getBusesValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.BUSES, compareResults);
    }

    @Override
    protected String getSvcsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", "reactivePowerSetpoint", "voltageSetpoint"),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint)));
    }

    @Override
    protected String getSvcsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", "reactivePowerSetpoint", "voltageSetpoint", AbstractValidationFormatterWriter.CONNECTED,
                                       "regulationMode", "bMin", "bMax", AbstractValidationFormatterWriter.VALIDATION),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getSvcsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", "reactivePowerSetpoint", "voltageSetpoint",
                                       "p" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "reactivePowerSetpoint" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "voltageSetpoint" + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint),
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint)));
    }

    @Override
    protected String getSvcsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", "reactivePowerSetpoint", "voltageSetpoint",
                                       "p" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "reactivePowerSetpoint" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "voltageSetpoint" + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
                           String.join(";", otherSvcId,
                                       "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint)),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint),
                                       "", "", "", "", ""));
    }

    @Override
    protected String getSvcsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", "reactivePowerSetpoint", "voltageSetpoint", AbstractValidationFormatterWriter.CONNECTED,
                                       "regulationMode", "bMin", "bMax", AbstractValidationFormatterWriter.VALIDATION,
                                       "p" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "reactivePowerSetpoint" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "voltageSetpoint" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "regulationMode" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "bMin" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "bMax" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), AbstractValidationFormatterWriter.SUCCESS,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getSvcsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", "reactivePowerSetpoint", "voltageSetpoint", AbstractValidationFormatterWriter.CONNECTED,
                                       "regulationMode", "bMin", "bMax", AbstractValidationFormatterWriter.VALIDATION,
                                       "p" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "reactivePowerSetpoint" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "voltageSetpoint" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "regulationMode" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "bMin" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "bMax" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
                           String.join(";", otherSvcId,
                                       "", "", "", "", "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetpoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), AbstractValidationFormatterWriter.SUCCESS,
                                       "", "", "", "", "", "", "", "", "", ""));
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
                                       AbstractValidationFormatterWriter.CONNECTED, "qMax", "nominalV", AbstractValidationFormatterWriter.VALIDATION),
                           String.join(";", shuntId,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", p), Integer.toString(currentSectionCount),
                                       Integer.toString(maximumSectionCount), String.format(Locale.getDefault(), "%g", bPerSection),
                                       String.format(Locale.getDefault(), "%g", v), Boolean.toString(connected),
                                       String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", nominalV),
                                       AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getShuntsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "q", "expectedQ",
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expectedQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
                           String.join(";", shuntId,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ)));
    }

    @Override
    protected String getShuntsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "q", "expectedQ",
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expectedQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
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
                                       AbstractValidationFormatterWriter.CONNECTED, "qMax", "nominalV", AbstractValidationFormatterWriter.VALIDATION,
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expectedQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "p" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "currentSectionCount" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "maximumSectionCount" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "bPerSection" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "qMax" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "nominalV" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
                           String.join(";", shuntId,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", p), Integer.toString(currentSectionCount),
                                       Integer.toString(maximumSectionCount), String.format(Locale.getDefault(), "%g", bPerSection),
                                       String.format(Locale.getDefault(), "%g", v), Boolean.toString(connected),
                                       String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", nominalV),
                                       AbstractValidationFormatterWriter.SUCCESS,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", p), Integer.toString(currentSectionCount),
                                       Integer.toString(maximumSectionCount), String.format(Locale.getDefault(), "%g", bPerSection),
                                       String.format(Locale.getDefault(), "%g", v), Boolean.toString(connected),
                                       String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", nominalV),
                                       AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getShuntsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "q", "expectedQ", "p", "currentSectionCount", "maximumSectionCount", "bPerSection", "v",
                                       AbstractValidationFormatterWriter.CONNECTED, "qMax", "nominalV", AbstractValidationFormatterWriter.VALIDATION,
                                       "q" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "expectedQ" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "p" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "currentSectionCount" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "maximumSectionCount" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "bPerSection" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "v" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.CONNECTED + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "qMax" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       "nominalV" + AbstractValidationFormatterWriter.POST_LF_SUFFIX,
                                       AbstractValidationFormatterWriter.VALIDATION + AbstractValidationFormatterWriter.POST_LF_SUFFIX),
                           String.join(";", otherShuntId,
                                       "", "", "", "", "", "", "", "", "", "", "",
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", p), Integer.toString(currentSectionCount),
                                       Integer.toString(maximumSectionCount), String.format(Locale.getDefault(), "%g", bPerSection),
                                       String.format(Locale.getDefault(), "%g", v), Boolean.toString(connected),
                                       String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", nominalV),
                                       AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", shuntId,
                                       String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", expectedQ),
                                       String.format(Locale.getDefault(), "%g", p), Integer.toString(currentSectionCount),
                                       Integer.toString(maximumSectionCount), String.format(Locale.getDefault(), "%g", bPerSection),
                                       String.format(Locale.getDefault(), "%g", v), Boolean.toString(connected),
                                       String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", nominalV),
                                       AbstractValidationFormatterWriter.SUCCESS,
                                       "", "", "", "", "", "", "", "", "", "", ""));
    }

    @Override
    protected ValidationWriter getShuntsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.SHUNTS, compareResults);
    }

}
