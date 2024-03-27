/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.Objects;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.ContainersMapping;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public abstract class AbstractConverter {

    AbstractConverter(ContainersMapping containersMapping, Network network) {
        this.containersMapping = Objects.requireNonNull(containersMapping);
        this.network = Objects.requireNonNull(network);
    }

    ContainersMapping getContainersMapping() {
        return containersMapping;
    }

    Network getNetwork() {
        return network;
    }

    static String getBusId(int busNum) {
        return "B" + busNum;
    }

    static Complex impedanceToEngineeringUnits(Complex impedance, double vnom, double sbase) {
        return impedance.multiply(vnom * vnom / sbase);
    }

    static double impedanceToEngineeringUnits(double impedance, double vnom, double sbase) {
        return impedance * vnom * vnom / sbase;
    }

    static double impedanceToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(double impedance, double vnom1, double vnom2, double sbase) {
        return impedance * vnom1 * vnom2 / sbase;
    }

    static Complex admittanceToEngineeringUnits(Complex admittance, double vnom, double sbase) {
        return admittance.multiply(sbase / (vnom * vnom));
    }

    static double admittanceEnd1ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(double admittanceTransmissionEu, double shuntAdmittance, double vnom1, double vnom2, double sbase) {
        return shuntAdmittance * sbase / (vnom1 * vnom1) - (1 - vnom2 / vnom1) * admittanceTransmissionEu;
    }

    static double admittanceEnd2ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(double admittanceTransmissionEu, double shuntAdmittance, double vnom1, double vnom2, double sbase) {
        return shuntAdmittance * sbase / (vnom2 * vnom2) - (1 - vnom1 / vnom2) * admittanceTransmissionEu;
    }

    static double admittanceToEngineeringUnits(double admittance, double vnom, double sbase) {
        return admittance * sbase / (vnom * vnom);
    }

    static double powerToShuntAdmittance(double power, double vnom) {
        return power / (vnom * vnom);
    }

    static double shuntAdmittanceToPower(double shuntAdmittance, double vnom) {
        return shuntAdmittance * vnom * vnom;
    }

    private final ContainersMapping containersMapping;
    private final Network network;
}
