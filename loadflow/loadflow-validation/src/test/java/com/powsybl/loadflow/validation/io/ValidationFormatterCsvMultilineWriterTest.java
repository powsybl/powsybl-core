/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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
                           String.join(";", branchId, "validation", "success"));
    }

    @Override
    protected ValidationWriter getFlowsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose) {
        return new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.FLOWS);
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
                           String.join(";", generatorId, "connected", Boolean.toString(connected)),
                           String.join(";", generatorId, "voltageRegulatorOn", Boolean.toString(voltageRegulatorOn)),
                           String.join(";", generatorId, "minQ", String.format(Locale.getDefault(), "%g", minQ)),
                           String.join(";", generatorId, "maxQ", String.format(Locale.getDefault(), "%g", maxQ)),
                           String.join(";", generatorId, "validation", "success"));
    }

    @Override
    protected ValidationWriter getGeneratorsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose) {
        return new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.GENERATORS);
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
                String.join(";", busId, "twtP", String.format(Locale.getDefault(), "%g", twtP)),
                String.join(";", busId, "twtQ", String.format(Locale.getDefault(), "%g", twtQ)),
                String.join(";", busId, "tltP", String.format(Locale.getDefault(), "%g", tltP)),
                String.join(";", busId, "tltQ", String.format(Locale.getDefault(), "%g", tltQ)),
                String.join(";", busId, AbstractValidationFormatterWriter.VALIDATION, "success"));
    }

    @Override
    protected ValidationWriter getBusesValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose) {
        return new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.BUSES);
    }

    @Override
    protected String getSvcsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "characteristic", "value"),
                           String.join(";", svcId, "p", String.format(Locale.getDefault(), "%g", -p)),
                           String.join(";", svcId, "q", String.format(Locale.getDefault(), "%g", -q)),
                           String.join(";", svcId, "v", String.format(Locale.getDefault(), "%g", v)),
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
                           String.join(";", svcId, "reactivePowerSetpoint", String.format(Locale.getDefault(), "%g", reactivePowerSetpoint)),
                           String.join(";", svcId, "voltageSetpoint", String.format(Locale.getDefault(), "%g", voltageSetpoint)),
                           String.join(";", svcId, "connected", Boolean.toString(connected)),
                           String.join(";", svcId, "regulationMode", regulationMode.name()),
                           String.join(";", svcId, "bMin", String.format(Locale.getDefault(), "%g", bMin)),
                           String.join(";", svcId, "bMax", String.format(Locale.getDefault(), "%g", bMax)),
                           String.join(";", svcId, "validation", "success"));
    }

    @Override
    protected ValidationWriter getSvcsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose) {
        return new ValidationFormatterCsvMultilineWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.SVCS);
    }

}
