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
                                       "u1", "u2", "theta1", "theta2", "z", "y", "ksi", "validation"),
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
                                       String.format(Locale.getDefault(), "%g", ksi), "success"));
    }

    @Override
    protected ValidationWriter getFlowsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.FLOWS);
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
                           String.join(";", "id", "p", "q", "v", "targetP", "targetQ", "targetV", "connected", "voltageRegulatorOn", "minQ", "maxQ", "validation"),
                           String.join(";", generatorId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", targetP),
                                       String.format(Locale.getDefault(), "%g", targetQ), String.format(Locale.getDefault(), "%g", targetV),
                                       Boolean.toString(connected), Boolean.toString(voltageRegulatorOn),
                                       String.format(Locale.getDefault(), "%g", minQ), String.format(Locale.getDefault(), "%g", maxQ), "success"));
    }

    @Override
    protected ValidationWriter getGeneratorsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.GENERATORS);
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
                            "vscCSP", "vscCSQ", "lineP", "lineQ", "twtP", "twtQ", "tltP", "tltQ", "validation"),
                String.join(";", busId,
                            String.format(Locale.getDefault(), "%g", incomingP), String.format(Locale.getDefault(), "%g", incomingQ),
                            String.format(Locale.getDefault(), "%g", loadP), String.format(Locale.getDefault(), "%g", loadQ),
                            String.format(Locale.getDefault(), "%g", genP), String.format(Locale.getDefault(), "%g", genQ),
                            String.format(Locale.getDefault(), "%g", shuntP), String.format(Locale.getDefault(), "%g", shuntQ),
                            String.format(Locale.getDefault(), "%g", svcP), String.format(Locale.getDefault(), "%g", svcQ),
                            String.format(Locale.getDefault(), "%g", vscCSP), String.format(Locale.getDefault(), "%g", vscCSQ),
                            String.format(Locale.getDefault(), "%g", lineP), String.format(Locale.getDefault(), "%g", lineQ),
                            String.format(Locale.getDefault(), "%g", twtP), String.format(Locale.getDefault(), "%g", twtQ),
                            String.format(Locale.getDefault(), "%g", tltP), String.format(Locale.getDefault(), "%g", tltQ), "success"));
    }

    @Override
    protected ValidationWriter getBusesValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.BUSES);
    }

    @Override
    protected String getSvcsContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", "reactivePowerSetPoint", "voltageSetPoint"),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetPoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetPoint)));
    }

    @Override
    protected String getSvcsVerboseContent() {
        return String.join(System.lineSeparator(),
                           "test " + ValidationType.SVCS + " check",
                           String.join(";", "id", "p", "q", "v", "reactivePowerSetPoint", "voltageSetPoint", "connected", "regulationMode", "bMin", "bMax", "validation"),
                           String.join(";", svcId,
                                       String.format(Locale.getDefault(), "%g", -p), String.format(Locale.getDefault(), "%g", -q),
                                       String.format(Locale.getDefault(), "%g", v), String.format(Locale.getDefault(), "%g", reactivePowerSetPoint),
                                       String.format(Locale.getDefault(), "%g", voltageSetPoint), Boolean.toString(connected),
                                       regulationMode.name(), String.format(Locale.getDefault(), "%g", bMin),
                                       String.format(Locale.getDefault(), "%g", bMax), "success"));
    }

    @Override
    protected ValidationWriter getSvcsValidationFormatterCsvWriter(TableFormatterConfig config, Writer writer, boolean verbose) {
        return new ValidationFormatterCsvWriter("test", CsvTableFormatterFactory.class, config, writer, verbose, ValidationType.SVCS);
    }

}
