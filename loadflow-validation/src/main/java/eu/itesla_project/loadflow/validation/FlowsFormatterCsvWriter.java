/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

import eu.itesla_project.commons.io.table.Column;
import eu.itesla_project.commons.io.table.TableFormatterConfig;
import eu.itesla_project.commons.io.table.TableFormatterFactory;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class FlowsFormatterCsvWriter extends FlowsFormatterWriter {

    private static TableFormatterConfig TABLE_FORMATTER_CONFIG = TableFormatterConfig.load();
    private final boolean verbose;

    public FlowsFormatterCsvWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass, 
                                   TableFormatterConfig formatterConfig, Writer writer, boolean verbose) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(formatterFactoryClass);
        Objects.requireNonNull(writer);
        this.verbose = verbose;
        formatter = createTableFormatter(id, formatterFactoryClass, formatterConfig, writer);
    }

    public FlowsFormatterCsvWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass, 
                                   Writer writer, boolean verbose) {
        this(id, formatterFactoryClass, TABLE_FORMATTER_CONFIG, writer, verbose);
    }

    protected Column[] getColumns() {
        if ( verbose ) {
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
                new Column("ksi")
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
    }

    @Override
    public void write(String branchId, double p1, double p1_calc, double q1, double q1_calc, double p2, double p2_calc, double q2, double q2_calc, 
                      double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2, 
                      double u1, double u2, double theta1, double theta2, double z, double y, double ksi) throws IOException {
        Objects.requireNonNull(branchId);
        formatter.writeCell(branchId)
                 .writeCell(p1)
                 .writeCell(p1_calc)
                 .writeCell(q1)
                 .writeCell(q1_calc)
                 .writeCell(p2)
                 .writeCell(p2_calc)
                 .writeCell(q2)
                 .writeCell(q2_calc);
        if ( verbose ) {
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
                     .writeCell(ksi);
        }
    }

}
