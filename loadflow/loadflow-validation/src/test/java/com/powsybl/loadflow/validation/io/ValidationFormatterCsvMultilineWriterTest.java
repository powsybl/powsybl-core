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
public class ValidationFormatterCsvMultilineWriterTest extends AbstractValidationFormatterWriterTest {

    @Override
    protected String getFlowsContent() {
        return String.join(System.lineSeparator(),
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
    }

    @Override
    protected String getFlowsVerboseContent() {
        return String.join(System.lineSeparator(),
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
                           String.join(";", branchId, "ksi", String.format(Locale.getDefault(), "%g", ksi)),
                           String.join(";", branchId, AbstractValidationFormatterWriter.CONNECTED + "1", Boolean.toString(connected1)),
                           String.join(";", branchId, AbstractValidationFormatterWriter.CONNECTED + "2", Boolean.toString(connected2)),
                           String.join(";", branchId, AbstractValidationFormatterWriter.MAIN_COMPONENT + "1", Boolean.toString(mainComponent1)),
                           String.join(";", branchId, AbstractValidationFormatterWriter.MAIN_COMPONENT + "2", Boolean.toString(mainComponent2)),
                           String.join(";", branchId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getFlowsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.FLOWS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", branchId, "network_p1", String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1)),
                           String.join(";", branchId, "expected_p1", String.format(Locale.getDefault(), "%g", p1Calc), String.format(Locale.getDefault(), "%g", p1Calc)),
                           String.join(";", branchId, "network_q1", String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1)),
                           String.join(";", branchId, "expected_q1", String.format(Locale.getDefault(), "%g", q1Calc), String.format(Locale.getDefault(), "%g", q1Calc)),
                           String.join(";", branchId, "network_p2", String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2)),
                           String.join(";", branchId, "expected_p2", String.format(Locale.getDefault(), "%g", p2Calc), String.format(Locale.getDefault(), "%g", p2Calc)),
                           String.join(";", branchId, "network_q2", String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2)),
                           String.join(";", branchId, "expected_q2", String.format(Locale.getDefault(), "%g", q2Calc), String.format(Locale.getDefault(), "%g", q2Calc)));
    }

    @Override
    protected String getFlowsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.FLOWS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherBranchId, "network_p1", "", String.format(Locale.getDefault(), "%g", p1)),
                           String.join(";", otherBranchId, "expected_p1", "", String.format(Locale.getDefault(), "%g", p1Calc)),
                           String.join(";", otherBranchId, "network_q1", "", String.format(Locale.getDefault(), "%g", q1)),
                           String.join(";", otherBranchId, "expected_q1", "", String.format(Locale.getDefault(), "%g", q1Calc)),
                           String.join(";", otherBranchId, "network_p2", "", String.format(Locale.getDefault(), "%g", p2)),
                           String.join(";", otherBranchId, "expected_p2", "", String.format(Locale.getDefault(), "%g", p2Calc)),
                           String.join(";", otherBranchId, "network_q2", "", String.format(Locale.getDefault(), "%g", q2)),
                           String.join(";", otherBranchId, "expected_q2", "", String.format(Locale.getDefault(), "%g", q2Calc)),
                           String.join(";", branchId, "network_p1", String.format(Locale.getDefault(), "%g", p1), ""),
                           String.join(";", branchId, "expected_p1", String.format(Locale.getDefault(), "%g", p1Calc), ""),
                           String.join(";", branchId, "network_q1", String.format(Locale.getDefault(), "%g", q1), ""),
                           String.join(";", branchId, "expected_q1", String.format(Locale.getDefault(), "%g", q1Calc), ""),
                           String.join(";", branchId, "network_p2", String.format(Locale.getDefault(), "%g", p2), ""),
                           String.join(";", branchId, "expected_p2", String.format(Locale.getDefault(), "%g", p2Calc), ""),
                           String.join(";", branchId, "network_q2", String.format(Locale.getDefault(), "%g", q2), ""),
                           String.join(";", branchId, "expected_q2", String.format(Locale.getDefault(), "%g", q2Calc), ""));
    }

    @Override
    protected String getFlowsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.FLOWS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", branchId, "network_p1", String.format(Locale.getDefault(), "%g", p1), String.format(Locale.getDefault(), "%g", p1)),
                           String.join(";", branchId, "expected_p1", String.format(Locale.getDefault(), "%g", p1Calc), String.format(Locale.getDefault(), "%g", p1Calc)),
                           String.join(";", branchId, "network_q1", String.format(Locale.getDefault(), "%g", q1), String.format(Locale.getDefault(), "%g", q1)),
                           String.join(";", branchId, "expected_q1", String.format(Locale.getDefault(), "%g", q1Calc), String.format(Locale.getDefault(), "%g", q1Calc)),
                           String.join(";", branchId, "network_p2", String.format(Locale.getDefault(), "%g", p2), String.format(Locale.getDefault(), "%g", p2)),
                           String.join(";", branchId, "expected_p2", String.format(Locale.getDefault(), "%g", p2Calc), String.format(Locale.getDefault(), "%g", p2Calc)),
                           String.join(";", branchId, "network_q2", String.format(Locale.getDefault(), "%g", q2), String.format(Locale.getDefault(), "%g", q2)),
                           String.join(";", branchId, "expected_q2", String.format(Locale.getDefault(), "%g", q2Calc), String.format(Locale.getDefault(), "%g", q2Calc)),
                           String.join(";", branchId, "r", String.format(Locale.getDefault(), "%g", r), String.format(Locale.getDefault(), "%g", r)),
                           String.join(";", branchId, "x", String.format(Locale.getDefault(), "%g", x), String.format(Locale.getDefault(), "%g", x)),
                           String.join(";", branchId, "g1", String.format(Locale.getDefault(), "%g", g1), String.format(Locale.getDefault(), "%g", g1)),
                           String.join(";", branchId, "g2", String.format(Locale.getDefault(), "%g", g2), String.format(Locale.getDefault(), "%g", g2)),
                           String.join(";", branchId, "b1", String.format(Locale.getDefault(), "%g", b1), String.format(Locale.getDefault(), "%g", b1)),
                           String.join(";", branchId, "b2", String.format(Locale.getDefault(), "%g", b2), String.format(Locale.getDefault(), "%g", b2)),
                           String.join(";", branchId, "rho1", String.format(Locale.getDefault(), "%g", rho1), String.format(Locale.getDefault(), "%g", rho1)),
                           String.join(";", branchId, "rho2", String.format(Locale.getDefault(), "%g", rho2), String.format(Locale.getDefault(), "%g", rho2)),
                           String.join(";", branchId, "alpha1", String.format(Locale.getDefault(), "%g", alpha1), String.format(Locale.getDefault(), "%g", alpha1)),
                           String.join(";", branchId, "alpha2", String.format(Locale.getDefault(), "%g", alpha2), String.format(Locale.getDefault(), "%g", alpha2)),
                           String.join(";", branchId, "u1", String.format(Locale.getDefault(), "%g", u1), String.format(Locale.getDefault(), "%g", u1)),
                           String.join(";", branchId, "u2", String.format(Locale.getDefault(), "%g", u2), String.format(Locale.getDefault(), "%g", u2)),
                           String.join(";", branchId, "theta1", String.format(Locale.getDefault(), "%g", theta1), String.format(Locale.getDefault(), "%g", theta1)),
                           String.join(";", branchId, "theta2", String.format(Locale.getDefault(), "%g", theta2), String.format(Locale.getDefault(), "%g", theta2)),
                           String.join(";", branchId, "z", String.format(Locale.getDefault(), "%g", z), String.format(Locale.getDefault(), "%g", z)),
                           String.join(";", branchId, "y", String.format(Locale.getDefault(), "%g", y), String.format(Locale.getDefault(), "%g", y)),
                           String.join(";", branchId, "ksi", String.format(Locale.getDefault(), "%g", ksi), String.format(Locale.getDefault(), "%g", ksi)),
                           String.join(";", branchId, AbstractValidationFormatterWriter.CONNECTED + "1", Boolean.toString(connected1), Boolean.toString(connected1)),
                           String.join(";", branchId, AbstractValidationFormatterWriter.CONNECTED + "2", Boolean.toString(connected2), Boolean.toString(connected2)),
                           String.join(";", branchId, AbstractValidationFormatterWriter.MAIN_COMPONENT + "1", Boolean.toString(mainComponent1), Boolean.toString(mainComponent1)),
                           String.join(";", branchId, AbstractValidationFormatterWriter.MAIN_COMPONENT + "2", Boolean.toString(mainComponent2), Boolean.toString(mainComponent2)),
                           String.join(";", branchId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getFlowsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.FLOWS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherBranchId, "network_p1", "", String.format(Locale.getDefault(), "%g", p1)),
                           String.join(";", otherBranchId, "expected_p1", "", String.format(Locale.getDefault(), "%g", p1Calc)),
                           String.join(";", otherBranchId, "network_q1", "", String.format(Locale.getDefault(), "%g", q1)),
                           String.join(";", otherBranchId, "expected_q1", "", String.format(Locale.getDefault(), "%g", q1Calc)),
                           String.join(";", otherBranchId, "network_p2", "", String.format(Locale.getDefault(), "%g", p2)),
                           String.join(";", otherBranchId, "expected_p2", "", String.format(Locale.getDefault(), "%g", p2Calc)),
                           String.join(";", otherBranchId, "network_q2", "", String.format(Locale.getDefault(), "%g", q2)),
                           String.join(";", otherBranchId, "expected_q2", "", String.format(Locale.getDefault(), "%g", q2Calc)),
                           String.join(";", otherBranchId, "r", "", String.format(Locale.getDefault(), "%g", r)),
                           String.join(";", otherBranchId, "x", "", String.format(Locale.getDefault(), "%g", x)),
                           String.join(";", otherBranchId, "g1", "", String.format(Locale.getDefault(), "%g", g1)),
                           String.join(";", otherBranchId, "g2", "", String.format(Locale.getDefault(), "%g", g2)),
                           String.join(";", otherBranchId, "b1", "", String.format(Locale.getDefault(), "%g", b1)),
                           String.join(";", otherBranchId, "b2", "", String.format(Locale.getDefault(), "%g", b2)),
                           String.join(";", otherBranchId, "rho1", "", String.format(Locale.getDefault(), "%g", rho1)),
                           String.join(";", otherBranchId, "rho2", "", String.format(Locale.getDefault(), "%g", rho2)),
                           String.join(";", otherBranchId, "alpha1", "", String.format(Locale.getDefault(), "%g", alpha1)),
                           String.join(";", otherBranchId, "alpha2", "", String.format(Locale.getDefault(), "%g", alpha2)),
                           String.join(";", otherBranchId, "u1", "", String.format(Locale.getDefault(), "%g", u1)),
                           String.join(";", otherBranchId, "u2", "", String.format(Locale.getDefault(), "%g", u2)),
                           String.join(";", otherBranchId, "theta1", "", String.format(Locale.getDefault(), "%g", theta1)),
                           String.join(";", otherBranchId, "theta2", "", String.format(Locale.getDefault(), "%g", theta2)),
                           String.join(";", otherBranchId, "z", "", String.format(Locale.getDefault(), "%g", z)),
                           String.join(";", otherBranchId, "y", "", String.format(Locale.getDefault(), "%g", y)),
                           String.join(";", otherBranchId, "ksi", "", String.format(Locale.getDefault(), "%g", ksi)),
                           String.join(";", otherBranchId, AbstractValidationFormatterWriter.CONNECTED + "1", "", Boolean.toString(connected1)),
                           String.join(";", otherBranchId, AbstractValidationFormatterWriter.CONNECTED + "2", "", Boolean.toString(connected2)),
                           String.join(";", otherBranchId, AbstractValidationFormatterWriter.MAIN_COMPONENT + "1", "", Boolean.toString(mainComponent1)),
                           String.join(";", otherBranchId, AbstractValidationFormatterWriter.MAIN_COMPONENT + "2", "", Boolean.toString(mainComponent2)),
                           String.join(";", otherBranchId, AbstractValidationFormatterWriter.VALIDATION, "", AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", branchId, "network_p1", String.format(Locale.getDefault(), "%g", p1), ""),
                           String.join(";", branchId, "expected_p1", String.format(Locale.getDefault(), "%g", p1Calc), ""),
                           String.join(";", branchId, "network_q1", String.format(Locale.getDefault(), "%g", q1), ""),
                           String.join(";", branchId, "expected_q1", String.format(Locale.getDefault(), "%g", q1Calc), ""),
                           String.join(";", branchId, "network_p2", String.format(Locale.getDefault(), "%g", p2), ""),
                           String.join(";", branchId, "expected_p2", String.format(Locale.getDefault(), "%g", p2Calc), ""),
                           String.join(";", branchId, "network_q2", String.format(Locale.getDefault(), "%g", q2), ""),
                           String.join(";", branchId, "expected_q2", String.format(Locale.getDefault(), "%g", q2Calc), ""),
                           String.join(";", branchId, "r", String.format(Locale.getDefault(), "%g", r), ""),
                           String.join(";", branchId, "x", String.format(Locale.getDefault(), "%g", x), ""),
                           String.join(";", branchId, "g1", String.format(Locale.getDefault(), "%g", g1), ""),
                           String.join(";", branchId, "g2", String.format(Locale.getDefault(), "%g", g2), ""),
                           String.join(";", branchId, "b1", String.format(Locale.getDefault(), "%g", b1), ""),
                           String.join(";", branchId, "b2", String.format(Locale.getDefault(), "%g", b2), ""),
                           String.join(";", branchId, "rho1", String.format(Locale.getDefault(), "%g", rho1), ""),
                           String.join(";", branchId, "rho2", String.format(Locale.getDefault(), "%g", rho2), ""),
                           String.join(";", branchId, "alpha1", String.format(Locale.getDefault(), "%g", alpha1), ""),
                           String.join(";", branchId, "alpha2", String.format(Locale.getDefault(), "%g", alpha2), ""),
                           String.join(";", branchId, "u1", String.format(Locale.getDefault(), "%g", u1), ""),
                           String.join(";", branchId, "u2", String.format(Locale.getDefault(), "%g", u2), ""),
                           String.join(";", branchId, "theta1", String.format(Locale.getDefault(), "%g", theta1), ""),
                           String.join(";", branchId, "theta2", String.format(Locale.getDefault(), "%g", theta2), ""),
                           String.join(";", branchId, "z", String.format(Locale.getDefault(), "%g", z), ""),
                           String.join(";", branchId, "y", String.format(Locale.getDefault(), "%g", y), ""),
                           String.join(";", branchId, "ksi", String.format(Locale.getDefault(), "%g", ksi), ""),
                           String.join(";", branchId, AbstractValidationFormatterWriter.CONNECTED + "1", Boolean.toString(connected1), ""),
                           String.join(";", branchId, AbstractValidationFormatterWriter.CONNECTED + "2", Boolean.toString(connected2), ""),
                           String.join(";", branchId, AbstractValidationFormatterWriter.MAIN_COMPONENT + "1", Boolean.toString(mainComponent1), ""),
                           String.join(";", branchId, AbstractValidationFormatterWriter.MAIN_COMPONENT + "2", Boolean.toString(mainComponent2), ""),
                           String.join(";", branchId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, ""));
    }

    @Override
    protected ValidationWriter getFlowsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.FLOWS, compareResults);
    }

    @Override
    protected String getGeneratorsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", generatorId, "p", String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", generatorId, "q", String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", generatorId, "v", String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", generatorId, "targetP", String.format(Locale.getDefault(), "%g", targetP)),
                           String.join(";", generatorId, "targetQ", String.format(Locale.getDefault(), "%g", targetQ)),
                           String.join(";", generatorId, "targetV", String.format(Locale.getDefault(), "%g", targetV)));
    }

    @Override
    protected String getGeneratorsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", generatorId, "p", String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", generatorId, "q", String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", generatorId, "v", String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", generatorId, "targetP", String.format(Locale.getDefault(), "%g", targetP)),
                           String.join(";", generatorId, "targetQ", String.format(Locale.getDefault(), "%g", targetQ)),
                           String.join(";", generatorId, "targetV", String.format(Locale.getDefault(), "%g", targetV)),
                           String.join(";", generatorId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected)),
                           String.join(";", generatorId, "voltageRegulatorOn", Boolean.toString(voltageRegulatorOn)),
                           String.join(";", generatorId, "minP", String.format(Locale.getDefault(), "%g", minP)),
                           String.join(";", generatorId, "maxP", String.format(Locale.getDefault(), "%g", maxP)),
                           String.join(";", generatorId, "minQ", String.format(Locale.getDefault(), "%g", minQ)),
                           String.join(";", generatorId, "maxQ", String.format(Locale.getDefault(), "%g", maxQ)),
                           String.join(";", generatorId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent)),
                           String.join(";", generatorId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getGeneratorsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", generatorId, "p", String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", generatorId, "q", String.format(Locale.getDefault(), "%g", -q), String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", generatorId, "v", String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", generatorId, "targetP", String.format(Locale.getDefault(), "%g", targetP), String.format(Locale.getDefault(), "%g", targetP)),
                           String.join(";", generatorId, "targetQ", String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetQ)),
                           String.join(";", generatorId, "targetV", String.format(Locale.getDefault(), "%g", targetV), String.format(Locale.getDefault(), "%g", targetV)));
    }

    @Override
    protected String getGeneratorsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherGeneratorId, "p", "", String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", otherGeneratorId, "q", "", String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", otherGeneratorId, "v", "", String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", otherGeneratorId, "targetP", "", String.format(Locale.getDefault(), "%g", targetP)),
                           String.join(";", otherGeneratorId, "targetQ", "", String.format(Locale.getDefault(), "%g", targetQ)),
                           String.join(";", otherGeneratorId, "targetV", "", String.format(Locale.getDefault(), "%g", targetV)),
                           String.join(";", generatorId, "p", String.format(Locale.getDefault(), "%g", -p), ""),
                           String.join(";", generatorId, "q", String.format(Locale.getDefault(), "%g", -q), ""),
                           String.join(";", generatorId, "v", String.format(Locale.getDefault(), "%g", v), ""),
                           String.join(";", generatorId, "targetP", String.format(Locale.getDefault(), "%g", targetP), ""),
                           String.join(";", generatorId, "targetQ", String.format(Locale.getDefault(), "%g", targetQ), ""),
                           String.join(";", generatorId, "targetV", String.format(Locale.getDefault(), "%g", targetV), ""));
    }

    @Override
    protected String getGeneratorsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", generatorId, "p", String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", generatorId, "q", String.format(Locale.getDefault(), "%g", -q), String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", generatorId, "v", String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", generatorId, "targetP", String.format(Locale.getDefault(), "%g", targetP), String.format(Locale.getDefault(), "%g", targetP)),
                           String.join(";", generatorId, "targetQ", String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetQ)),
                           String.join(";", generatorId, "targetV", String.format(Locale.getDefault(), "%g", targetV), String.format(Locale.getDefault(), "%g", targetV)),
                           String.join(";", generatorId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected), Boolean.toString(connected)),
                           String.join(";", generatorId, "voltageRegulatorOn", Boolean.toString(voltageRegulatorOn), Boolean.toString(voltageRegulatorOn)),
                           String.join(";", generatorId, "minP", String.format(Locale.getDefault(), "%g", minP), String.format(Locale.getDefault(), "%g", minP)),
                           String.join(";", generatorId, "maxP", String.format(Locale.getDefault(), "%g", maxP), String.format(Locale.getDefault(), "%g", maxP)),
                           String.join(";", generatorId, "minQ", String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", minQ)),
                           String.join(";", generatorId, "maxQ", String.format(Locale.getDefault(), "%g", maxQ), String.format(Locale.getDefault(), "%g", maxQ)),
                           String.join(";", generatorId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent), Boolean.toString(mainComponent)),
                           String.join(";", generatorId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getGeneratorsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.GENERATORS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherGeneratorId, "p", "", String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", otherGeneratorId, "q", "", String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", otherGeneratorId, "v", "", String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", otherGeneratorId, "targetP", "", String.format(Locale.getDefault(), "%g", targetP)),
                           String.join(";", otherGeneratorId, "targetQ", "", String.format(Locale.getDefault(), "%g", targetQ)),
                           String.join(";", otherGeneratorId, "targetV", "", String.format(Locale.getDefault(), "%g", targetV)),
                           String.join(";", otherGeneratorId, AbstractValidationFormatterWriter.CONNECTED, "", Boolean.toString(connected)),
                           String.join(";", otherGeneratorId, "voltageRegulatorOn", "", Boolean.toString(voltageRegulatorOn)),
                           String.join(";", otherGeneratorId, "minP", "", String.format(Locale.getDefault(), "%g", minP)),
                           String.join(";", otherGeneratorId, "maxP", "", String.format(Locale.getDefault(), "%g", maxP)),
                           String.join(";", otherGeneratorId, "minQ", "", String.format(Locale.getDefault(), "%g", minQ)),
                           String.join(";", otherGeneratorId, "maxQ", "", String.format(Locale.getDefault(), "%g", maxQ)),
                           String.join(";", otherGeneratorId, AbstractValidationFormatterWriter.MAIN_COMPONENT, "", Boolean.toString(mainComponent)),
                           String.join(";", otherGeneratorId, AbstractValidationFormatterWriter.VALIDATION, "", AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", generatorId, "p", String.format(Locale.getDefault(), "%g", -p), ""),
                           String.join(";", generatorId, "q", String.format(Locale.getDefault(), "%g", -q), ""),
                           String.join(";", generatorId, "v", String.format(Locale.getDefault(), "%g", v), ""),
                           String.join(";", generatorId, "targetP", String.format(Locale.getDefault(), "%g", targetP), ""),
                           String.join(";", generatorId, "targetQ", String.format(Locale.getDefault(), "%g", targetQ), ""),
                           String.join(";", generatorId, "targetV", String.format(Locale.getDefault(), "%g", targetV), ""),
                           String.join(";", generatorId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected), ""),
                           String.join(";", generatorId, "voltageRegulatorOn", Boolean.toString(voltageRegulatorOn), ""),
                           String.join(";", generatorId, "minP", String.format(Locale.getDefault(), "%g", minP), ""),
                           String.join(";", generatorId, "maxP", String.format(Locale.getDefault(), "%g", maxP), ""),
                           String.join(";", generatorId, "minQ", String.format(Locale.getDefault(), "%g", minQ), ""),
                           String.join(";", generatorId, "maxQ", String.format(Locale.getDefault(), "%g", maxQ), ""),
                           String.join(";", generatorId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent), ""),
                           String.join(";", generatorId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, ""));
    }

    @Override
    protected ValidationWriter getGeneratorsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.GENERATORS, compareResults);
    }

    @Override
    protected String getBusesContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", busId, "incomingP", String.format(Locale.getDefault(), "%g", incomingP)),
                           String.join(";", busId, "incomingQ", String.format(Locale.getDefault(), "%g", incomingQ)),
                           String.join(";", busId, "loadP", String.format(Locale.getDefault(), "%g", loadP)),
                           String.join(";", busId, "loadQ", String.format(Locale.getDefault(), "%g", loadQ)));
    }

    @Override
    protected String getBusesVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", busId, "incomingP", String.format(Locale.getDefault(), "%g", incomingP)),
                           String.join(";", busId, "incomingQ", String.format(Locale.getDefault(), "%g", incomingQ)),
                           String.join(";", busId, "loadP", String.format(Locale.getDefault(), "%g", loadP)),
                           String.join(";", busId, "loadQ", String.format(Locale.getDefault(), "%g", loadQ)),
                           String.join(";", busId, "genP", String.format(Locale.getDefault(), "%g", genP)),
                           String.join(";", busId, "genQ", String.format(Locale.getDefault(), "%g", genQ)),
                           String.join(";", busId, "shuntP", String.format(Locale.getDefault(), "%g", shuntP)),
                           String.join(";", busId, "shuntQ", String.format(Locale.getDefault(), "%g", shuntQ)),
                           String.join(";", busId, "svcP", String.format(Locale.getDefault(), "%g", svcP)),
                           String.join(";", busId, "svcQ", String.format(Locale.getDefault(), "%g", svcQ)),
                           String.join(";", busId, "vscCSP", String.format(Locale.getDefault(), "%g", vscCSP)),
                           String.join(";", busId, "vscCSQ", String.format(Locale.getDefault(), "%g", vscCSQ)),
                           String.join(";", busId, "lineP", String.format(Locale.getDefault(), "%g", lineP)),
                           String.join(";", busId, "lineQ", String.format(Locale.getDefault(), "%g", lineQ)),
                           String.join(";", busId, "danglingLineP", String.format(Locale.getDefault(), "%g", danglingLineP)),
                           String.join(";", busId, "danglingLineQ", String.format(Locale.getDefault(), "%g", danglingLineQ)),
                           String.join(";", busId, "twtP", String.format(Locale.getDefault(), "%g", twtP)),
                           String.join(";", busId, "twtQ", String.format(Locale.getDefault(), "%g", twtQ)),
                           String.join(";", busId, "tltP", String.format(Locale.getDefault(), "%g", tltP)),
                           String.join(";", busId, "tltQ", String.format(Locale.getDefault(), "%g", tltQ)),
                           String.join(";", busId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent)),
                           String.join(";", busId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getBusesCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", busId, "incomingP", String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingP)),
                           String.join(";", busId, "incomingQ", String.format(Locale.getDefault(), "%g", incomingQ), String.format(Locale.getDefault(), "%g", incomingQ)),
                           String.join(";", busId, "loadP", String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadP)),
                           String.join(";", busId, "loadQ", String.format(Locale.getDefault(), "%g", loadQ), String.format(Locale.getDefault(), "%g", loadQ)));
    }

    @Override
    protected String getBusesCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherBusId, "incomingP", "", String.format(Locale.getDefault(), "%g", incomingP)),
                           String.join(";", otherBusId, "incomingQ", "", String.format(Locale.getDefault(), "%g", incomingQ)),
                           String.join(";", otherBusId, "loadP", "", String.format(Locale.getDefault(), "%g", loadP)),
                           String.join(";", otherBusId, "loadQ", "", String.format(Locale.getDefault(), "%g", loadQ)),
                           String.join(";", busId, "incomingP", String.format(Locale.getDefault(), "%g", incomingP), ""),
                           String.join(";", busId, "incomingQ", String.format(Locale.getDefault(), "%g", incomingQ), ""),
                           String.join(";", busId, "loadP", String.format(Locale.getDefault(), "%g", loadP), ""),
                           String.join(";", busId, "loadQ", String.format(Locale.getDefault(), "%g", loadQ), ""));
    }

    @Override
    protected String getBusesCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", busId, "incomingP", String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingP)),
                           String.join(";", busId, "incomingQ", String.format(Locale.getDefault(), "%g", incomingQ), String.format(Locale.getDefault(), "%g", incomingQ)),
                           String.join(";", busId, "loadP", String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadP)),
                           String.join(";", busId, "loadQ", String.format(Locale.getDefault(), "%g", loadQ), String.format(Locale.getDefault(), "%g", loadQ)),
                           String.join(";", busId, "genP", String.format(Locale.getDefault(), "%g", genP), String.format(Locale.getDefault(), "%g", genP)),
                           String.join(";", busId, "genQ", String.format(Locale.getDefault(), "%g", genQ), String.format(Locale.getDefault(), "%g", genQ)),
                           String.join(";", busId, "shuntP", String.format(Locale.getDefault(), "%g", shuntP), String.format(Locale.getDefault(), "%g", shuntP)),
                           String.join(";", busId, "shuntQ", String.format(Locale.getDefault(), "%g", shuntQ), String.format(Locale.getDefault(), "%g", shuntQ)),
                           String.join(";", busId, "svcP", String.format(Locale.getDefault(), "%g", svcP), String.format(Locale.getDefault(), "%g", svcP)),
                           String.join(";", busId, "svcQ", String.format(Locale.getDefault(), "%g", svcQ), String.format(Locale.getDefault(), "%g", svcQ)),
                           String.join(";", busId, "vscCSP", String.format(Locale.getDefault(), "%g", vscCSP), String.format(Locale.getDefault(), "%g", vscCSP)),
                           String.join(";", busId, "vscCSQ", String.format(Locale.getDefault(), "%g", vscCSQ), String.format(Locale.getDefault(), "%g", vscCSQ)),
                           String.join(";", busId, "lineP", String.format(Locale.getDefault(), "%g", lineP), String.format(Locale.getDefault(), "%g", lineP)),
                           String.join(";", busId, "lineQ", String.format(Locale.getDefault(), "%g", lineQ), String.format(Locale.getDefault(), "%g", lineQ)),
                           String.join(";", busId, "danglingLineP", String.format(Locale.getDefault(), "%g", danglingLineP), String.format(Locale.getDefault(), "%g", danglingLineP)),
                           String.join(";", busId, "danglingLineQ", String.format(Locale.getDefault(), "%g", danglingLineQ), String.format(Locale.getDefault(), "%g", danglingLineQ)),
                           String.join(";", busId, "twtP", String.format(Locale.getDefault(), "%g", twtP), String.format(Locale.getDefault(), "%g", twtP)),
                           String.join(";", busId, "twtQ", String.format(Locale.getDefault(), "%g", twtQ), String.format(Locale.getDefault(), "%g", twtQ)),
                           String.join(";", busId, "tltP", String.format(Locale.getDefault(), "%g", tltP), String.format(Locale.getDefault(), "%g", tltP)),
                           String.join(";", busId, "tltQ", String.format(Locale.getDefault(), "%g", tltQ), String.format(Locale.getDefault(), "%g", tltQ)),
                           String.join(";", busId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent), Boolean.toString(mainComponent)),
                           String.join(";", busId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getBusesCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.BUSES + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherBusId, "incomingP", "", String.format(Locale.getDefault(), "%g", incomingP)),
                           String.join(";", otherBusId, "incomingQ", "", String.format(Locale.getDefault(), "%g", incomingQ)),
                           String.join(";", otherBusId, "loadP", "", String.format(Locale.getDefault(), "%g", loadP)),
                           String.join(";", otherBusId, "loadQ", "", String.format(Locale.getDefault(), "%g", loadQ)),
                           String.join(";", otherBusId, "genP", "", String.format(Locale.getDefault(), "%g", genP)),
                           String.join(";", otherBusId, "genQ", "", String.format(Locale.getDefault(), "%g", genQ)),
                           String.join(";", otherBusId, "shuntP", "", String.format(Locale.getDefault(), "%g", shuntP)),
                           String.join(";", otherBusId, "shuntQ", "", String.format(Locale.getDefault(), "%g", shuntQ)),
                           String.join(";", otherBusId, "svcP", "", String.format(Locale.getDefault(), "%g", svcP)),
                           String.join(";", otherBusId, "svcQ", "", String.format(Locale.getDefault(), "%g", svcQ)),
                           String.join(";", otherBusId, "vscCSP", "", String.format(Locale.getDefault(), "%g", vscCSP)),
                           String.join(";", otherBusId, "vscCSQ", "", String.format(Locale.getDefault(), "%g", vscCSQ)),
                           String.join(";", otherBusId, "lineP", "", String.format(Locale.getDefault(), "%g", lineP)),
                           String.join(";", otherBusId, "lineQ", "", String.format(Locale.getDefault(), "%g", lineQ)),
                           String.join(";", otherBusId, "danglingLineP", "", String.format(Locale.getDefault(), "%g", danglingLineP)),
                           String.join(";", otherBusId, "danglingLineQ", "", String.format(Locale.getDefault(), "%g", danglingLineQ)),
                           String.join(";", otherBusId, "twtP", "", String.format(Locale.getDefault(), "%g", twtP)),
                           String.join(";", otherBusId, "twtQ", "", String.format(Locale.getDefault(), "%g", twtQ)),
                           String.join(";", otherBusId, "tltP", "", String.format(Locale.getDefault(), "%g", tltP)),
                           String.join(";", otherBusId, "tltQ", "", String.format(Locale.getDefault(), "%g", tltQ)),
                           String.join(";", otherBusId, AbstractValidationFormatterWriter.MAIN_COMPONENT, "", Boolean.toString(mainComponent)),
                           String.join(";", otherBusId, AbstractValidationFormatterWriter.VALIDATION, "", AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", busId, "incomingP", String.format(Locale.getDefault(), "%g", incomingP), ""),
                           String.join(";", busId, "incomingQ", String.format(Locale.getDefault(), "%g", incomingQ), ""),
                           String.join(";", busId, "loadP", String.format(Locale.getDefault(), "%g", loadP), ""),
                           String.join(";", busId, "loadQ", String.format(Locale.getDefault(), "%g", loadQ), ""),
                           String.join(";", busId, "genP", String.format(Locale.getDefault(), "%g", genP), ""),
                           String.join(";", busId, "genQ", String.format(Locale.getDefault(), "%g", genQ), ""),
                           String.join(";", busId, "shuntP", String.format(Locale.getDefault(), "%g", shuntP), ""),
                           String.join(";", busId, "shuntQ", String.format(Locale.getDefault(), "%g", shuntQ), ""),
                           String.join(";", busId, "svcP", String.format(Locale.getDefault(), "%g", svcP), ""),
                           String.join(";", busId, "svcQ", String.format(Locale.getDefault(), "%g", svcQ), ""),
                           String.join(";", busId, "vscCSP", String.format(Locale.getDefault(), "%g", vscCSP), ""),
                           String.join(";", busId, "vscCSQ", String.format(Locale.getDefault(), "%g", vscCSQ), ""),
                           String.join(";", busId, "lineP", String.format(Locale.getDefault(), "%g", lineP), ""),
                           String.join(";", busId, "lineQ", String.format(Locale.getDefault(), "%g", lineQ), ""),
                           String.join(";", busId, "danglingLineP", String.format(Locale.getDefault(), "%g", danglingLineP), ""),
                           String.join(";", busId, "danglingLineQ", String.format(Locale.getDefault(), "%g", danglingLineQ), ""),
                           String.join(";", busId, "twtP", String.format(Locale.getDefault(), "%g", twtP), ""),
                           String.join(";", busId, "twtQ", String.format(Locale.getDefault(), "%g", twtQ), ""),
                           String.join(";", busId, "tltP", String.format(Locale.getDefault(), "%g", tltP), ""),
                           String.join(";", busId, "tltQ", String.format(Locale.getDefault(), "%g", tltQ), ""),
                           String.join(";", busId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent), ""),
                           String.join(";", busId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, ""));
    }

    @Override
    protected ValidationWriter getBusesValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.BUSES, compareResults);
    }

    @Override
    protected String getSvcsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", svcId, "p", String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", svcId, "q", String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", svcId, "v", String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", svcId, AbstractValidationFormatterWriter.NOMINAL_V, String.format(Locale.getDefault(), "%g", nominalV)),
                           String.join(";", svcId, "reactivePowerSetpoint", String.format(Locale.getDefault(), "%g", reactivePowerSetpoint)),
                           String.join(";", svcId, "voltageSetpoint", String.format(Locale.getDefault(), "%g", voltageSetpoint)));
    }

    @Override
    protected String getSvcsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", svcId, "p", String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", svcId, "q", String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", svcId, "v", String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", svcId, AbstractValidationFormatterWriter.NOMINAL_V, String.format(Locale.getDefault(), "%g", nominalV)),
                           String.join(";", svcId, "reactivePowerSetpoint", String.format(Locale.getDefault(), "%g", reactivePowerSetpoint)),
                           String.join(";", svcId, "voltageSetpoint", String.format(Locale.getDefault(), "%g", voltageSetpoint)),
                           String.join(";", svcId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected)),
                           String.join(";", svcId, "regulationMode", regulationMode.name()),
                           String.join(";", svcId, "bMin", String.format(Locale.getDefault(), "%g", bMin)),
                           String.join(";", svcId, "bMax", String.format(Locale.getDefault(), "%g", bMax)),
                           String.join(";", svcId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent)),
                           String.join(";", svcId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getSvcsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", svcId, "p", String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", svcId, "q", String.format(Locale.getDefault(), "%g", -q), String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", svcId, "v", String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", svcId, AbstractValidationFormatterWriter.NOMINAL_V, String.format(Locale.getDefault(), "%g", nominalV), String.format(Locale.getDefault(), "%g", nominalV)),
                           String.join(";", svcId, "reactivePowerSetpoint", String.format(Locale.getDefault(), "%g", reactivePowerSetpoint), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint)),
                           String.join(";", svcId, "voltageSetpoint", String.format(Locale.getDefault(), "%g", voltageSetpoint), String.format(Locale.getDefault(), "%g", voltageSetpoint)));
    }

    @Override
    protected String getSvcsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherSvcId, "p", "", String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", otherSvcId, "q", "", String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", otherSvcId, "v", "", String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", otherSvcId, AbstractValidationFormatterWriter.NOMINAL_V, "", String.format(Locale.getDefault(), "%g", nominalV)),
                           String.join(";", otherSvcId, "reactivePowerSetpoint", "", String.format(Locale.getDefault(), "%g", reactivePowerSetpoint)),
                           String.join(";", otherSvcId, "voltageSetpoint", "", String.format(Locale.getDefault(), "%g", voltageSetpoint)),
                           String.join(";", svcId, "p", String.format(Locale.getDefault(), "%g", -p), ""),
                           String.join(";", svcId, "q", String.format(Locale.getDefault(), "%g", -q), ""),
                           String.join(";", svcId, "v", String.format(Locale.getDefault(), "%g", v), ""),
                           String.join(";", svcId, AbstractValidationFormatterWriter.NOMINAL_V, String.format(Locale.getDefault(), "%g", nominalV), ""),
                           String.join(";", svcId, "reactivePowerSetpoint", String.format(Locale.getDefault(), "%g", reactivePowerSetpoint), ""),
                           String.join(";", svcId, "voltageSetpoint", String.format(Locale.getDefault(), "%g", voltageSetpoint), ""));
    }

    @Override
    protected String getSvcsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", svcId, "p", String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", svcId, "q", String.format(Locale.getDefault(), "%g", -q), String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", svcId, "v", String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", svcId, AbstractValidationFormatterWriter.NOMINAL_V, String.format(Locale.getDefault(), "%g", nominalV), String.format(Locale.getDefault(), "%g", nominalV)),
                           String.join(";", svcId, "reactivePowerSetpoint", String.format(Locale.getDefault(), "%g", reactivePowerSetpoint), String.format(Locale.getDefault(), "%g", reactivePowerSetpoint)),
                           String.join(";", svcId, "voltageSetpoint", String.format(Locale.getDefault(), "%g", voltageSetpoint), String.format(Locale.getDefault(), "%g", voltageSetpoint)),
                           String.join(";", svcId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected), Boolean.toString(connected)),
                           String.join(";", svcId, "regulationMode", regulationMode.name(), regulationMode.name()),
                           String.join(";", svcId, "bMin", String.format(Locale.getDefault(), "%g", bMin), String.format(Locale.getDefault(), "%g", bMin)),
                           String.join(";", svcId, "bMax", String.format(Locale.getDefault(), "%g", bMax), String.format(Locale.getDefault(), "%g", bMax)),
                           String.join(";", svcId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent), Boolean.toString(mainComponent)),
                           String.join(";", svcId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getSvcsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherSvcId, "p", "", String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", otherSvcId, "q", "", String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", otherSvcId, "v", "", String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", otherSvcId, AbstractValidationFormatterWriter.NOMINAL_V, "", String.format(Locale.getDefault(), "%g", nominalV)),
                           String.join(";", otherSvcId, "reactivePowerSetpoint", "", String.format(Locale.getDefault(), "%g", reactivePowerSetpoint)),
                           String.join(";", otherSvcId, "voltageSetpoint", "", String.format(Locale.getDefault(), "%g", voltageSetpoint)),
                           String.join(";", otherSvcId, AbstractValidationFormatterWriter.CONNECTED, "", Boolean.toString(connected)),
                           String.join(";", otherSvcId, "regulationMode", "", regulationMode.name()),
                           String.join(";", otherSvcId, "bMin", "", String.format(Locale.getDefault(), "%g", bMin)),
                           String.join(";", otherSvcId, "bMax", "", String.format(Locale.getDefault(), "%g", bMax)),
                           String.join(";", otherSvcId, AbstractValidationFormatterWriter.MAIN_COMPONENT, "", Boolean.toString(mainComponent)),
                           String.join(";", otherSvcId, AbstractValidationFormatterWriter.VALIDATION, "", AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", svcId, "p", String.format(Locale.getDefault(), "%g", -p), ""),
                           String.join(";", svcId, "q", String.format(Locale.getDefault(), "%g", -q), ""),
                           String.join(";", svcId, "v", String.format(Locale.getDefault(), "%g", v), ""),
                           String.join(";", svcId, AbstractValidationFormatterWriter.NOMINAL_V, String.format(Locale.getDefault(), "%g", nominalV), ""),
                           String.join(";", svcId, "reactivePowerSetpoint", String.format(Locale.getDefault(), "%g", reactivePowerSetpoint), ""),
                           String.join(";", svcId, "voltageSetpoint", String.format(Locale.getDefault(), "%g", voltageSetpoint), ""),
                           String.join(";", svcId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected), ""),
                           String.join(";", svcId, "regulationMode", regulationMode.name(), ""),
                           String.join(";", svcId, "bMin", String.format(Locale.getDefault(), "%g", bMin), ""),
                           String.join(";", svcId, "bMax", String.format(Locale.getDefault(), "%g", bMax), ""),
                           String.join(";", svcId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent), ""),
                           String.join(";", svcId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, ""));
    }

    @Override
    protected ValidationWriter getSvcsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.SVCS, compareResults);
    }

    @Override
    protected String getShuntsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", shuntId, "q", String.format(Locale.getDefault(), "%g", q)),
                           String.join(";", shuntId, "expectedQ", String.format(Locale.getDefault(), "%g", expectedQ)));
    }

    @Override
    protected String getShuntsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", shuntId, "q", String.format(Locale.getDefault(), "%g", q)),
                           String.join(";", shuntId, "expectedQ", String.format(Locale.getDefault(), "%g", expectedQ)),
                           String.join(";", shuntId, "p", String.format(Locale.getDefault(), "%g", p)),
                           String.join(";", shuntId, "currentSectionCount", Integer.toString(currentSectionCount)),
                           String.join(";", shuntId, "maximumSectionCount", Integer.toString(maximumSectionCount)),
                           String.join(";", shuntId, "bPerSection", String.format(Locale.getDefault(), "%g", bPerSection)),
                           String.join(";", shuntId, "v", String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected)),
                           String.join(";", shuntId, "qMax", String.format(Locale.getDefault(), "%g", qMax)),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.NOMINAL_V, String.format(Locale.getDefault(), "%g", nominalV)),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent)),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getShuntsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", shuntId, "q", String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", q)),
                           String.join(";", shuntId, "expectedQ", String.format(Locale.getDefault(), "%g", expectedQ), String.format(Locale.getDefault(), "%g", expectedQ)));
    }

    @Override
    protected String getShuntsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherShuntId, "q", "", String.format(Locale.getDefault(), "%g", q)),
                           String.join(";", otherShuntId, "expectedQ", "", String.format(Locale.getDefault(), "%g", expectedQ)),
                           String.join(";", shuntId, "q", String.format(Locale.getDefault(), "%g", q), ""),
                           String.join(";", shuntId, "expectedQ", String.format(Locale.getDefault(), "%g", expectedQ), ""));
    }

    @Override
    protected String getShuntsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", shuntId, "q", String.format(Locale.getDefault(), "%g", q), String.format(Locale.getDefault(), "%g", q)),
                           String.join(";", shuntId, "expectedQ", String.format(Locale.getDefault(), "%g", expectedQ), String.format(Locale.getDefault(), "%g", expectedQ)),
                           String.join(";", shuntId, "p", String.format(Locale.getDefault(), "%g", p), String.format(Locale.getDefault(), "%g", p)),
                           String.join(";", shuntId, "currentSectionCount", Integer.toString(currentSectionCount), Integer.toString(currentSectionCount)),
                           String.join(";", shuntId, "maximumSectionCount", Integer.toString(maximumSectionCount), Integer.toString(maximumSectionCount)),
                           String.join(";", shuntId, "bPerSection", String.format(Locale.getDefault(), "%g", bPerSection), String.format(Locale.getDefault(), "%g", bPerSection)),
                           String.join(";", shuntId, "v", String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected), Boolean.toString(connected)),
                           String.join(";", shuntId, "qMax", String.format(Locale.getDefault(), "%g", qMax), String.format(Locale.getDefault(), "%g", qMax)),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.NOMINAL_V, String.format(Locale.getDefault(), "%g", nominalV), String.format(Locale.getDefault(), "%g", nominalV)),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent), Boolean.toString(mainComponent)),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getShuntsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SHUNTS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherShuntId, "q", "", String.format(Locale.getDefault(), "%g", q)),
                           String.join(";", otherShuntId, "expectedQ", "", String.format(Locale.getDefault(), "%g", expectedQ)),
                           String.join(";", otherShuntId, "p", "", String.format(Locale.getDefault(), "%g", p)),
                           String.join(";", otherShuntId, "currentSectionCount", "", Integer.toString(currentSectionCount)),
                           String.join(";", otherShuntId, "maximumSectionCount", "", Integer.toString(maximumSectionCount)),
                           String.join(";", otherShuntId, "bPerSection", "", String.format(Locale.getDefault(), "%g", bPerSection)),
                           String.join(";", otherShuntId, "v", "", String.format(Locale.getDefault(), "%g", v)),
                           String.join(";", otherShuntId, AbstractValidationFormatterWriter.CONNECTED, "", Boolean.toString(connected)),
                           String.join(";", otherShuntId, "qMax", "", String.format(Locale.getDefault(), "%g", qMax)),
                           String.join(";", otherShuntId, AbstractValidationFormatterWriter.NOMINAL_V, "", String.format(Locale.getDefault(), "%g", nominalV)),
                           String.join(";", otherShuntId, AbstractValidationFormatterWriter.MAIN_COMPONENT, "", Boolean.toString(mainComponent)),
                           String.join(";", otherShuntId, AbstractValidationFormatterWriter.VALIDATION, "", AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", shuntId, "q", String.format(Locale.getDefault(), "%g", q), ""),
                           String.join(";", shuntId, "expectedQ", String.format(Locale.getDefault(), "%g", expectedQ), ""),
                           String.join(";", shuntId, "p", String.format(Locale.getDefault(), "%g", p), ""),
                           String.join(";", shuntId, "currentSectionCount", Integer.toString(currentSectionCount), ""),
                           String.join(";", shuntId, "maximumSectionCount", Integer.toString(maximumSectionCount), ""),
                           String.join(";", shuntId, "bPerSection", String.format(Locale.getDefault(), "%g", bPerSection), ""),
                           String.join(";", shuntId, "v", String.format(Locale.getDefault(), "%g", v), ""),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected), ""),
                           String.join(";", shuntId, "qMax", String.format(Locale.getDefault(), "%g", qMax), ""),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.NOMINAL_V, String.format(Locale.getDefault(), "%g", nominalV), ""),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent), ""),
                           String.join(";", shuntId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, ""));
    }

    @Override
    protected ValidationWriter getShuntsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.SHUNTS, compareResults);
    }

    @Override
    protected String getTwtsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.TWTS + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", twtId, "error", String.format(Locale.getDefault(), "%g", error)),
                           String.join(";", twtId, "upIncrement", String.format(Locale.getDefault(), "%g", upIncrement)),
                           String.join(";", twtId, "downIncrement", String.format(Locale.getDefault(), "%g", downIncrement)));
    }

    @Override
    protected String getTwtsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.TWTS + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", twtId, "error", String.format(Locale.getDefault(), "%g", error)),
                           String.join(";", twtId, "upIncrement", String.format(Locale.getDefault(), "%g", upIncrement)),
                           String.join(";", twtId, "downIncrement", String.format(Locale.getDefault(), "%g", downIncrement)),
                           String.join(";", twtId, "rho", String.format(Locale.getDefault(), "%g", rho)),
                           String.join(";", twtId, "rhoPreviousStep", String.format(Locale.getDefault(), "%g", rhoPreviousStep)),
                           String.join(";", twtId, "rhoNextStep", String.format(Locale.getDefault(), "%g", rhoNextStep)),
                           String.join(";", twtId, "tapPosition", Integer.toString(tapPosition)),
                           String.join(";", twtId, "lowTapPosition", Integer.toString(lowTapPosition)),
                           String.join(";", twtId, "highTapPosition", Integer.toString(highTapPosition)),
                           String.join(";", twtId, "tapChangerTargetV", String.format(Locale.getDefault(), "%g", twtTargetV)),
                           String.join(";", twtId, "regulatedSide", regulatedSide.name()),
                           String.join(";", twtId, "v", String.format(Locale.getDefault(), "%g", twtV)),
                           String.join(";", twtId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected)),
                           String.join(";", twtId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent)),
                           String.join(";", twtId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getTwtsCompareContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.TWTS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", twtId, "error", String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", error)),
                           String.join(";", twtId, "upIncrement", String.format(Locale.getDefault(), "%g", upIncrement), String.format(Locale.getDefault(), "%g", upIncrement)),
                           String.join(";", twtId, "downIncrement", String.format(Locale.getDefault(), "%g", downIncrement), String.format(Locale.getDefault(), "%g", downIncrement)));
    }

    @Override
    protected String getTwtsCompareDifferentIdsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.TWTS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherTwtId, "error", "", String.format(Locale.getDefault(), "%g", error)),
                           String.join(";", otherTwtId, "upIncrement", "", String.format(Locale.getDefault(), "%g", upIncrement)),
                           String.join(";", otherTwtId, "downIncrement", "", String.format(Locale.getDefault(), "%g", downIncrement)),
                           String.join(";", twtId, "error", String.format(Locale.getDefault(), "%g", error), ""),
                           String.join(";", twtId, "upIncrement", String.format(Locale.getDefault(), "%g", upIncrement), ""),
                           String.join(";", twtId, "downIncrement", String.format(Locale.getDefault(), "%g", downIncrement), ""));
    }

    @Override
    protected String getTwtsCompareVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.TWTS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", twtId, "error", String.format(Locale.getDefault(), "%g", error), String.format(Locale.getDefault(), "%g", error)),
                           String.join(";", twtId, "upIncrement", String.format(Locale.getDefault(), "%g", upIncrement), String.format(Locale.getDefault(), "%g", upIncrement)),
                           String.join(";", twtId, "downIncrement", String.format(Locale.getDefault(), "%g", downIncrement), String.format(Locale.getDefault(), "%g", downIncrement)),
                           String.join(";", twtId, "rho", String.format(Locale.getDefault(), "%g", rho), String.format(Locale.getDefault(), "%g", rho)),
                           String.join(";", twtId, "rhoPreviousStep", String.format(Locale.getDefault(), "%g", rhoPreviousStep), String.format(Locale.getDefault(), "%g", rhoPreviousStep)),
                           String.join(";", twtId, "rhoNextStep", String.format(Locale.getDefault(), "%g", rhoNextStep), String.format(Locale.getDefault(), "%g", rhoNextStep)),
                           String.join(";", twtId, "tapPosition", Integer.toString(tapPosition), Integer.toString(tapPosition)),
                           String.join(";", twtId, "lowTapPosition", Integer.toString(lowTapPosition), Integer.toString(lowTapPosition)),
                           String.join(";", twtId, "highTapPosition", Integer.toString(highTapPosition), Integer.toString(highTapPosition)),
                           String.join(";", twtId, "tapChangerTargetV", String.format(Locale.getDefault(), "%g", twtTargetV), String.format(Locale.getDefault(), "%g", twtTargetV)),
                           String.join(";", twtId, "regulatedSide", regulatedSide.name(), regulatedSide.name()),
                           String.join(";", twtId, "v", String.format(Locale.getDefault(), "%g", twtV), String.format(Locale.getDefault(), "%g", twtV)),
                           String.join(";", twtId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected), Boolean.toString(connected)),
                           String.join(";", twtId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent), Boolean.toString(mainComponent)),
                           String.join(";", twtId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, AbstractValidationFormatterWriter.SUCCESS));
    }

    @Override
    protected String getTwtsCompareDifferentIdsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.TWTS + " check",
                           String.join(";", "id", "characteristic", "value", "value" + AbstractValidationFormatterWriter.POST_COMPUTATION_SUFFIX),
                           String.join(";", otherTwtId, "error", "", String.format(Locale.getDefault(), "%g", error)),
                           String.join(";", otherTwtId, "upIncrement", "", String.format(Locale.getDefault(), "%g", upIncrement)),
                           String.join(";", otherTwtId, "downIncrement", "", String.format(Locale.getDefault(), "%g", downIncrement)),
                           String.join(";", otherTwtId, "rho", "", String.format(Locale.getDefault(), "%g", rho)),
                           String.join(";", otherTwtId, "rhoPreviousStep", "", String.format(Locale.getDefault(), "%g", rhoPreviousStep)),
                           String.join(";", otherTwtId, "rhoNextStep", "", String.format(Locale.getDefault(), "%g", rhoNextStep)),
                           String.join(";", otherTwtId, "tapPosition", "", Integer.toString(tapPosition)),
                           String.join(";", otherTwtId, "lowTapPosition", "", Integer.toString(lowTapPosition)),
                           String.join(";", otherTwtId, "highTapPosition", "", Integer.toString(highTapPosition)),
                           String.join(";", otherTwtId, "tapChangerTargetV", "", String.format(Locale.getDefault(), "%g", twtTargetV)),
                           String.join(";", otherTwtId, "regulatedSide", "", regulatedSide.name()),
                           String.join(";", otherTwtId, "v", "", String.format(Locale.getDefault(), "%g", twtV)),
                           String.join(";", otherTwtId, AbstractValidationFormatterWriter.CONNECTED, "", Boolean.toString(connected)),
                           String.join(";", otherTwtId, AbstractValidationFormatterWriter.MAIN_COMPONENT, "", Boolean.toString(mainComponent)),
                           String.join(";", otherTwtId, AbstractValidationFormatterWriter.VALIDATION, "", AbstractValidationFormatterWriter.SUCCESS),
                           String.join(";", twtId, "error", String.format(Locale.getDefault(), "%g", error), ""),
                           String.join(";", twtId, "upIncrement", String.format(Locale.getDefault(), "%g", upIncrement), ""),
                           String.join(";", twtId, "downIncrement", String.format(Locale.getDefault(), "%g", downIncrement), ""),
                           String.join(";", twtId, "rho", String.format(Locale.getDefault(), "%g", rho), ""),
                           String.join(";", twtId, "rhoPreviousStep", String.format(Locale.getDefault(), "%g", rhoPreviousStep), ""),
                           String.join(";", twtId, "rhoNextStep", String.format(Locale.getDefault(), "%g", rhoNextStep), ""),
                           String.join(";", twtId, "tapPosition", Integer.toString(tapPosition), ""),
                           String.join(";", twtId, "lowTapPosition", Integer.toString(lowTapPosition), ""),
                           String.join(";", twtId, "highTapPosition", Integer.toString(highTapPosition), ""),
                           String.join(";", twtId, "tapChangerTargetV", String.format(Locale.getDefault(), "%g", twtTargetV), ""),
                           String.join(";", twtId, "regulatedSide", regulatedSide.name(), ""),
                           String.join(";", twtId, "v", String.format(Locale.getDefault(), "%g", twtV), ""),
                           String.join(";", twtId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(connected), ""),
                           String.join(";", twtId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(mainComponent), ""),
                           String.join(";", twtId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS, ""));
    }

    @Override
    protected ValidationWriter getTwtsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose, boolean compareResults) {
        return new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.TWTS, compareResults);
    }

    @Override
    protected String getTwtsMissingSideContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.TWTS + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", twtId, "error", "inv"),
                           String.join(";", twtId, "upIncrement", "inv"),
                           String.join(";", twtId, "downIncrement", "inv"),
                           String.join(";", twtId, "rho", String.format(Locale.getDefault(), "%g", rho)),
                           String.join(";", twtId, "rhoPreviousStep", String.format(Locale.getDefault(), "%g", rhoPreviousStep)),
                           String.join(";", twtId, "rhoNextStep", String.format(Locale.getDefault(), "%g", rhoNextStep)),
                           String.join(";", twtId, "tapPosition", Integer.toString(tapPosition)),
                           String.join(";", twtId, "lowTapPosition", Integer.toString(lowTapPosition)),
                           String.join(";", twtId, "highTapPosition", Integer.toString(highTapPosition)),
                           String.join(";", twtId, "tapChangerTargetV", String.format(Locale.getDefault(), "%g", twtTargetV)),
                           String.join(";", twtId, "regulatedSide", "inv"),
                           String.join(";", twtId, "v", "inv"),
                           String.join(";", twtId, AbstractValidationFormatterWriter.CONNECTED, Boolean.toString(false)),
                           String.join(";", twtId, AbstractValidationFormatterWriter.MAIN_COMPONENT, Boolean.toString(false)),
                           String.join(";", twtId, AbstractValidationFormatterWriter.VALIDATION, AbstractValidationFormatterWriter.SUCCESS));
    }

}
