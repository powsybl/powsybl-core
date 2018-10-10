/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
class StaticVarCompensatorExtension {

    static void setVoltageSetPoint(StaticVarCompensator self, double value) {
        self.setVoltageSetpoint(value)
    }

    static double getVoltageSetPoint(StaticVarCompensator self) {
        return self.getVoltageSetpoint()
    }

    static void setReactivePowerSetPoint(StaticVarCompensator self, double value) {
        self.setReactivePowerSetpoint()
    }

    static double getReactivePowerSetPoint(StaticVarCompensator self) {
        return self.getReactivePowerSetpoint()
    }
}
