/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcConverter<I extends DcConverter<I>> extends Connectable<I>, DcConnectable<I> {

    /**
     * Control Mode of the DC converter
     */
    enum ControlMode {
        /**
         * Controlling active power at Point of Common Coupling
         */
        P_PCC,
        /**
         * Controlling DC Voltage
         */
        V_DC
    }

    /**
     * Get the first AC terminal.
     */
    Terminal getTerminal1();

    /**
     * Get the optional second AC terminal.
     */
    Optional<Terminal> getTerminal2();

    /**
     * Get the side the AC terminal is connected to.
     */
    TwoSides getSide(Terminal terminal);

    /**
     * Get the AC terminal at provided side.
     */
    Terminal getTerminal(TwoSides side);

    /**
     * Get the first DC terminal.
     */
    DcTerminal getDcTerminal1();

    /**
     * Get the second DC terminal.
     */
    DcTerminal getDcTerminal2();

    /**
     * Get the side the DC terminal is connected to.
     */
    TwoSides getSide(DcTerminal dcTerminal);

    /**
     * Get the DC terminal at provided side.
     */
    DcTerminal getDcTerminal(TwoSides side);

    /**
     * Set the idle loss (MW).
     */
    I setIdleLoss(double idleLoss);

    /**
     * Get the idle loss (MW).
     */
    double getIdleLoss();

    /**
     * Set the switching loss (MW/A).
     */
    I setSwitchingLoss(double switchingLoss);

    /**
     * Get the switching loss (MW/A).
     */
    double getSwitchingLoss();

    /**
     * Set the resistive loss (&#937;).
     */
    I setResistiveLoss(double resistiveLoss);

    /**
     * Get the resistive loss (&#937;).
     */
    double getResistiveLoss();

    /**
     * Set the point of common coupling terminal
     */
    I setPccTerminal(Terminal pccTerminal);

    /**
     * Get the point of common coupling terminal
     */
    Terminal getPccTerminal();

    /**
     * Set the control mode of the converter
     */
    I setControlMode(ControlMode controlMode);

    /**
     * Get the control mode of the converter
     */
    ControlMode getControlMode();

    /**
     * Set the target active power at point of common coupling (MW)
     */
    I setTargetP(double targetP);

    /**
     * Get the target active power at point of common coupling (MW)
     */
    double getTargetP();

    /**
     * Set the target DC voltage (kV DC)
     */
    I setTargetVdc(double targetVdc);

    /**
     * Get the target DC voltage (kV DC)
     */
    double getTargetVdc();
}
