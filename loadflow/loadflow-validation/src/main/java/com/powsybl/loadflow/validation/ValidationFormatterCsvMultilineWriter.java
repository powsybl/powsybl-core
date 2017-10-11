/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class ValidationFormatterCsvMultilineWriter extends AbstractValidationFormatterWriter {

    private final boolean verbose;

    public ValidationFormatterCsvMultilineWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                            TableFormatterConfig formatterConfig, Writer writer, boolean verbose,
                                            ValidationType validationType) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(formatterFactoryClass);
        Objects.requireNonNull(writer);
        this.verbose = verbose;
        Objects.requireNonNull(validationType);
        formatter = createTableFormatter(id, formatterFactoryClass, formatterConfig, writer, validationType);
    }

    public ValidationFormatterCsvMultilineWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                            Writer writer, boolean verbose, ValidationType validationType) {
        this(id, formatterFactoryClass, TableFormatterConfig.load(), writer, verbose, validationType);
    }

    protected Column[] getColumns() {
        return new Column[] {
            new Column("id"),
            new Column("characteristic"),
            new Column("value")
        };
    }

    @Override
    public void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                      double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                      double u1, double u2, double theta1, double theta2, double z, double y, double ksi, boolean validated) throws IOException {
        Objects.requireNonNull(branchId);
        formatter.writeCell(branchId).writeCell("network_p1").writeCell(p1)
                 .writeCell(branchId).writeCell("expected_p1").writeCell(p1Calc)
                 .writeCell(branchId).writeCell("network_q1").writeCell(q1)
                 .writeCell(branchId).writeCell("expected_q1").writeCell(q1Calc)
                 .writeCell(branchId).writeCell("network_p2").writeCell(p2)
                 .writeCell(branchId).writeCell("expected_p2").writeCell(p2Calc)
                 .writeCell(branchId).writeCell("network_q2").writeCell(q2)
                 .writeCell(branchId).writeCell("expected_q2").writeCell(q2Calc);
        if (verbose) {
            formatter.writeCell(branchId).writeCell("r").writeCell(r)
                     .writeCell(branchId).writeCell("x").writeCell(x)
                     .writeCell(branchId).writeCell("g1").writeCell(g1)
                     .writeCell(branchId).writeCell("g2").writeCell(g2)
                     .writeCell(branchId).writeCell("b1").writeCell(b1)
                     .writeCell(branchId).writeCell("b2").writeCell(b2)
                     .writeCell(branchId).writeCell("rho1").writeCell(rho1)
                     .writeCell(branchId).writeCell("rho2").writeCell(rho2)
                     .writeCell(branchId).writeCell("alpha1").writeCell(alpha1)
                     .writeCell(branchId).writeCell("alpha2").writeCell(alpha2)
                     .writeCell(branchId).writeCell("u1").writeCell(u1)
                     .writeCell(branchId).writeCell("u2").writeCell(u2)
                     .writeCell(branchId).writeCell("theta1").writeCell(theta1)
                     .writeCell(branchId).writeCell("theta2").writeCell(theta2)
                     .writeCell(branchId).writeCell("z").writeCell(z)
                     .writeCell(branchId).writeCell("y").writeCell(y)
                     .writeCell(branchId).writeCell("ksi").writeCell(ksi)
                     .writeCell(branchId).writeCell("validation").writeCell(validated ? "success" : "fail");
        }
    }

    @Override
    public void write(String generatorId, float p, float q, float v, float targetP, float targetQ, float targetV,
                      boolean connected, boolean voltageRegulatorOn, float minQ, float maxQ, boolean validated) throws IOException {
        Objects.requireNonNull(generatorId);
        formatter.writeCell(generatorId).writeCell("p").writeCell(-p)
                 .writeCell(generatorId).writeCell("q").writeCell(-q)
                 .writeCell(generatorId).writeCell("v").writeCell(v)
                 .writeCell(generatorId).writeCell("targetP").writeCell(targetP)
                 .writeCell(generatorId).writeCell("targetQ").writeCell(targetQ)
                 .writeCell(generatorId).writeCell("targetV").writeCell(targetV);
        if (verbose) {
            formatter.writeCell(generatorId).writeCell("connected").writeCell(connected)
                     .writeCell(generatorId).writeCell("voltageRegulatorOn").writeCell(voltageRegulatorOn)
                     .writeCell(generatorId).writeCell("minQ").writeCell(minQ)
                     .writeCell(generatorId).writeCell("maxQ").writeCell(maxQ)
                     .writeCell(generatorId).writeCell("validation").writeCell(validated ? "success" : "fail");
        }
    }

}
