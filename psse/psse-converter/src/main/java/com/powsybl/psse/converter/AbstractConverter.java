/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.ContainersMapping;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractConverter {

    public AbstractConverter(ContainersMapping containersMapping, Network network) {
        this.containersMapping = containersMapping;
        this.network = network;
    }

    public ContainersMapping getContainersMapping() {
        return containersMapping;
    }

    public Network getNetwork() {
        return network;
    }

    public static String getBusId(int busNum) {
        return "B" + busNum;
    }

    public static Complex impedanceToEngineeringUnits(Complex impedance, double vnom, double sbase) {
        return impedance.multiply(vnom * vnom / sbase);
    }

    public static double impedanceToEngineeringUnits(double impedance, double vnom, double sbase) {
        return impedance * vnom * vnom / sbase;
    }

    public static Complex admittanceToEngineeringUnits(Complex admittance, double vnom, double sbase) {
        return admittance.multiply(sbase / (vnom * vnom));
    }

    public static double admittanceToEngineeringUnits(double admittance, double vnom, double sbase) {
        return admittance * sbase / (vnom * vnom);
    }

    public static double powerToShuntAdmittance(double power, double vnom) {
        return power / (vnom * vnom);
    }

    private final ContainersMapping containersMapping;
    private final Network network;
}
