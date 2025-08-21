/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation.io;

import java.io.IOException;

import com.powsybl.iidm.network.TwoSides;

import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.util.TwtData;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.it>}
 */
public interface ValidationWriter extends AutoCloseable {

    void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
               double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
               double u1, double u2, double theta1, double theta2, double z, double y, double ksi, int phaseAngleClock, boolean connected1, boolean connected2,
               boolean mainComponent1, boolean mainComponent2, boolean validated) throws IOException;

    void write(String generatorId, double p, double q, double v, double targetP, double targetQ, double targetV, double expectedP, boolean connected,
               boolean voltageRegulatorOn, double minP, double maxP, double minQ, double maxQ, boolean mainComponent, boolean validated) throws IOException;

    void write(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ, double batP, double batQ,
               double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
               double danglingLineP, double danglingLineQ, double twtP, double twtQ, double tltP, double tltQ, boolean mainComponent,
               boolean validated) throws IOException;

    void write(String svcId, double p, double q, double vControlled, double vController, double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
               boolean connected, RegulationMode regulationMode, boolean regulating, double bMin, double bMax, boolean mainComponent, boolean validated) throws IOException;

    void write(String shuntId, double q, double expectedQ, double p, int currentSectionCount, int maximumSectionCount, double bPerSection,
               double v, boolean connected, double qMax, double nominalV, boolean mainComponent, boolean validated) throws IOException;

    void write(String twtId, double error, double upIncrement, double downIncrement, double rho, double rhoPreviousStep, double rhoNextStep,
               int tapPosition, int lowTapPosition, int highTapPosition, double targetV, TwoSides regulatedSide, double v, boolean connected,
               boolean mainComponent, boolean validated) throws IOException;

    void write(String twtId, TwtData twtData, boolean validated) throws IOException;

    void setValidationCompleted();

    @Override
    void close() throws IOException;

}
