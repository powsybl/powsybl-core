/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.loadflow.validation.ValidationType;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class ValidationFormatterCsvWriter extends AbstractValidationFormatterWriter {

    private final boolean verbose;
    private final ValidationType validationType;

    public ValidationFormatterCsvWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                   TableFormatterConfig formatterConfig, Writer writer, boolean verbose,
                                   ValidationType validationType) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(formatterFactoryClass);
        Objects.requireNonNull(writer);
        this.verbose = verbose;
        this.validationType = Objects.requireNonNull(validationType);
        formatter = createTableFormatter(id, formatterFactoryClass, formatterConfig, writer, validationType);
    }

    public ValidationFormatterCsvWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                   Writer writer, boolean verbose, ValidationType validationType) {
        this(id, formatterFactoryClass, TableFormatterConfig.load(), writer, verbose, validationType);
    }

    protected Column[] getColumns() {
        switch (validationType) {
            case FLOWS:
                if (verbose) {
                    return new Column[] {
                        new Column("id"),
                        new Column("network_p1"),
                        new Column("expected_p1"),
                        new Column("network_q1"),
                        new Column("expected_q1"),
                        new Column("network_p2"),
                        new Column("expected_p2"),
                        new Column("network_q2"),
                        new Column("expected_q2"),
                        new Column("r"),
                        new Column("x"),
                        new Column("g1"),
                        new Column("g2"),
                        new Column("b1"),
                        new Column("b2"),
                        new Column("rho1"),
                        new Column("rho2"),
                        new Column("alpha1"),
                        new Column("alpha2"),
                        new Column("u1"),
                        new Column("u2"),
                        new Column("theta1"),
                        new Column("theta2"),
                        new Column("z"),
                        new Column("y"),
                        new Column("ksi"),
                        new Column(VALIDATION)
                    };
                }
                return new Column[] {
                    new Column("id"),
                    new Column("network_p1"),
                    new Column("expected_p1"),
                    new Column("network_q1"),
                    new Column("expected_q1"),
                    new Column("network_p2"),
                    new Column("expected_p2"),
                    new Column("network_q2"),
                    new Column("expected_q2")
                };
            case GENERATORS:
                if (verbose) {
                    return new Column[] {
                        new Column("id"),
                        new Column("p"),
                        new Column("q"),
                        new Column("v"),
                        new Column("targetP"),
                        new Column("targetQ"),
                        new Column("targetV"),
                        new Column("connected"),
                        new Column("voltageRegulatorOn"),
                        new Column("minQ"),
                        new Column("maxQ"),
                        new Column(VALIDATION)
                    };
                }
                return new Column[] {
                    new Column("id"),
                    new Column("p"),
                    new Column("q"),
                    new Column("v"),
                    new Column("targetP"),
                    new Column("targetQ"),
                    new Column("targetV")
                };
            case BUSES:
                if (verbose) {
                    return new Column[] {
                        new Column("id"),
                        new Column("incomingP"),
                        new Column("incomingQ"),
                        new Column("loadP"),
                        new Column("loadQ"),
                        new Column("genP"),
                        new Column("genQ"),
                        new Column("shuntP"),
                        new Column("shuntQ"),
                        new Column("svcP"),
                        new Column("svcQ"),
                        new Column("vscCSP"),
                        new Column("vscCSQ"),
                        new Column("lineP"),
                        new Column("lineQ"),
                        new Column("twtP"),
                        new Column("twtQ"),
                        new Column("tltP"),
                        new Column("tltQ"),
                        new Column(VALIDATION)
                    };
                }
                return new Column[] {
                    new Column("id"),
                    new Column("incomingP"),
                    new Column("incomingQ"),
                    new Column("loadP"),
                    new Column("loadQ")
                };
            case SVCS:
                if (verbose) {
                    return new Column[] {
                        new Column("id"),
                        new Column("p"),
                        new Column("q"),
                        new Column("v"),
                        new Column("reactivePowerSetpoint"),
                        new Column("voltageSetpoint"),
                        new Column("connected"),
                        new Column("regulationMode"),
                        new Column("bMin"),
                        new Column("bMax"),
                        new Column(VALIDATION)
                    };
                }
                return new Column[] {
                    new Column("id"),
                    new Column("p"),
                    new Column("q"),
                    new Column("v"),
                    new Column("reactivePowerSetpoint"),
                    new Column("voltageSetpoint")
                };
            default:
                throw new AssertionError("Unexpected ValidationType value: " + validationType);
        }
    }

    @Override
    public void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                      double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                      double u1, double u2, double theta1, double theta2, double z, double y, double ksi, boolean validated) throws IOException {
        Objects.requireNonNull(branchId);
        formatter.writeCell(branchId)
                 .writeCell(p1)
                 .writeCell(p1Calc)
                 .writeCell(q1)
                 .writeCell(q1Calc)
                 .writeCell(p2)
                 .writeCell(p2Calc)
                 .writeCell(q2)
                 .writeCell(q2Calc);
        if (verbose) {
            formatter.writeCell(r)
                     .writeCell(x)
                     .writeCell(g1)
                     .writeCell(g2)
                     .writeCell(b1)
                     .writeCell(b2)
                     .writeCell(rho1)
                     .writeCell(rho2)
                     .writeCell(alpha1)
                     .writeCell(alpha2)
                     .writeCell(u1)
                     .writeCell(u2)
                     .writeCell(theta1)
                     .writeCell(theta2)
                     .writeCell(z)
                     .writeCell(y)
                     .writeCell(ksi)
                     .writeCell(validated ? SUCCESS : FAIL);
        }
    }

    @Override
    public void write(String generatorId, float p, float q, float v, float targetP, float targetQ, float targetV,
                      boolean connected, boolean voltageRegulatorOn, float minQ, float maxQ, boolean validated) throws IOException {
        Objects.requireNonNull(generatorId);
        formatter.writeCell(generatorId)
                 .writeCell(-p)
                 .writeCell(-q)
                 .writeCell(v)
                 .writeCell(targetP)
                 .writeCell(targetQ)
                 .writeCell(targetV);
        if (verbose) {
            formatter.writeCell(connected)
                     .writeCell(voltageRegulatorOn)
                     .writeCell(minQ)
                     .writeCell(maxQ)
                     .writeCell(validated ? SUCCESS : FAIL);
        }
    }

    @Override
    public void write(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ,
                      double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                      double twtP, double twtQ, double tltP, double tltQ, boolean validated) throws IOException {
        Objects.requireNonNull(busId);
        formatter.writeCell(busId)
                 .writeCell(incomingP)
                 .writeCell(incomingQ)
                 .writeCell(loadP)
                 .writeCell(loadQ);
        if (verbose) {
            formatter.writeCell(genP)
                     .writeCell(genQ)
                     .writeCell(shuntP)
                     .writeCell(shuntQ)
                     .writeCell(svcP)
                     .writeCell(svcQ)
                     .writeCell(vscCSP)
                     .writeCell(vscCSQ)
                     .writeCell(lineP)
                     .writeCell(lineQ)
                     .writeCell(twtP)
                     .writeCell(twtQ)
                     .writeCell(tltP)
                     .writeCell(tltQ)
                     .writeCell(validated ? SUCCESS : FAIL);
        }
    }

    @Override
    public void write(String svcId, float p, float q, float v, float reactivePowerSetpoint, float voltageSetpoint,
                      boolean connected, RegulationMode regulationMode, float bMin, float bMax, boolean validated) throws IOException {
        Objects.requireNonNull(svcId);
        formatter.writeCell(svcId)
                 .writeCell(-p)
                 .writeCell(-q)
                 .writeCell(v)
                 .writeCell(reactivePowerSetpoint)
                 .writeCell(voltageSetpoint);
        if (verbose) {
            formatter.writeCell(connected)
                     .writeCell(regulationMode.name())
                     .writeCell(bMin)
                     .writeCell(bMax)
                     .writeCell(validated ? SUCCESS : FAIL);
        }
    }

}
