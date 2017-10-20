/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation.io;

import java.io.IOException;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public interface ValidationWriter extends AutoCloseable {

    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";
    public static final String VALIDATION = "validation";

    void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
               double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
               double u1, double u2, double theta1, double theta2, double z, double y, double ksi, boolean validated) throws IOException;

    void write(String generatorId, float p, float q, float v, float targetP, float targetQ, float targetV,
               boolean connected, boolean voltageRegulatorOn, float minQ, float maxQ, boolean validated) throws IOException;

    void write(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ,
               double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
               double twtP, double twtQ, double tltP, double tltQ, boolean validated) throws IOException;

    @Override
    void close() throws IOException;

}
