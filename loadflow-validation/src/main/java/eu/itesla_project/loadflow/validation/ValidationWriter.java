/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import java.io.IOException;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public interface ValidationWriter extends AutoCloseable {

    void write(String branchId, double p1, double p1_calc, double q1, double q1_calc, double p2, double p2_calc, double q2, double q2_calc, 
               double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2, 
               double u1, double u2, double theta1, double theta2, double z, double y, double ksi) throws IOException;

    void write(String generatorId, float p, float q, float v, float targetP, float targetQ, float targetV, 
               boolean connected, boolean voltageRegulatorOn, float minQ, float maxQ) throws IOException;

    @Override
    void close() throws IOException;

}
