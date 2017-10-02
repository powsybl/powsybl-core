/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import java.io.IOException;
import java.io.Writer;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public abstract class AbstractValidationFormatterWriter implements ValidationWriter {

    protected TableFormatter formatter;

    protected TableFormatter createTableFormatter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                                  TableFormatterConfig formatterConfig, Writer writer, ValidationType validationType) {
        try {
            TableFormatterFactory factory = formatterFactoryClass.newInstance();
            return factory.create(writer, id + " " + validationType + " check", formatterConfig, getColumns());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected abstract Column[] getColumns();

    @Override
    public abstract void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                               double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                               double u1, double u2, double theta1, double theta2, double z, double y, double ksi) throws IOException;

    @Override
    public abstract void write(String generatorId, float p, float q, float v, float targetP, float targetQ, float targetV,
                               boolean connected, boolean voltageRegulatorOn, float minQ, float maxQ) throws IOException;

    @Override
    public void close() throws IOException {
        formatter.close();
    }

}
