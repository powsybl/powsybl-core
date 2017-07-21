/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import eu.itesla_project.commons.io.table.Column;
import eu.itesla_project.commons.io.table.TableFormatterConfig;
import eu.itesla_project.commons.io.table.TableFormatterFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class FlowsFormatterCsvMultilineWriter extends FlowsFormatterWriter {

    private final boolean verbose;

    public FlowsFormatterCsvMultilineWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass, 
                                             TableFormatterConfig formatterConfig, Writer writer, boolean verbose) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(formatterFactoryClass);
        Objects.requireNonNull(writer);
        this.verbose = verbose;
        formatter = createTableFormatter(id, formatterFactoryClass, formatterConfig, writer);
    }

    public FlowsFormatterCsvMultilineWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass, 
                                             Writer writer, boolean verbose) {
        this(id, formatterFactoryClass, TableFormatterConfig.load(), writer, verbose);
    }

    protected Column[] getColumns() {
        return new Column[] {
                new Column("id"),
                new Column("characteristic"),
                new Column("value")
            };
    }

    @Override
    public void write(String branchId, double p1, double p1_calc, double q1, double q1_calc, double p2, double p2_calc, double q2, double q2_calc, 
                      double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2, 
                      double u1, double u2, double theta1, double theta2, double z, double y, double ksi) throws IOException {
        Objects.requireNonNull(branchId);
        formatter.writeCell(branchId).writeCell("network_p1").writeCell(p1)
                 .writeCell(branchId).writeCell("expected_p1").writeCell(p1_calc)
                 .writeCell(branchId).writeCell("network_q1").writeCell(q1)
                 .writeCell(branchId).writeCell("expected_q1").writeCell(q1_calc)
                 .writeCell(branchId).writeCell("network_p2").writeCell(p2)
                 .writeCell(branchId).writeCell("expected_p2").writeCell(p2_calc)
                 .writeCell(branchId).writeCell("network_q2").writeCell(q2)
                 .writeCell(branchId).writeCell("expected_q2").writeCell(q2_calc);
        if ( verbose ) {
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
                     .writeCell(branchId).writeCell("ksi").writeCell(ksi);
        }
    }

}
