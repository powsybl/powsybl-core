/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcVoltageSourceConverter extends DcConverter<DcVoltageSourceConverter>, ReactiveLimitsHolder {

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.DC_VOLTAGE_SOURCE_CONVERTER;
    }

    /**
     * Check if voltage regulator is on.
     * @return true if voltage regulator is on, false otherwise
     */
    boolean isVoltageRegulatorOn();

    /**
     * Set voltage regulator status.
     * @param voltageRegulatorOn the new voltage regulator status
     * @return the converter itself to allow method chaining
     */
    DcVoltageSourceConverter setVoltageRegulatorOn(boolean voltageRegulatorOn);

    /**
     * Get the voltage setpoint (Kv).
     * @return the voltage setpoint
     */
    double getVoltageSetpoint();

    /**
     * Set the voltage setpoint (Kv).
     * @param voltageSetpoint the voltage setpoint
     * @return the converter itself to allow method chaining
     */
    DcVoltageSourceConverter setVoltageSetpoint(double voltageSetpoint);

    /**
     * Get the reactive power setpoint (MVar).
     * @return the reactive power setpoint
     */
    double getReactivePowerSetpoint();

    /**
     * Set the reactive power setpoint (MVar).
     * @param reactivePowerSetpoint the reactive power setpoint
     * @return the converter itself to allow method chaining
     */
    DcVoltageSourceConverter setReactivePowerSetpoint(double reactivePowerSetpoint);

    /**
     * Get the terminal used for regulation.
     * @return the terminal used for regulation
     */
    Terminal getRegulatingTerminal();

    DcVoltageSourceConverter setRegulatingTerminal(Terminal regulatingTerminal);
}
