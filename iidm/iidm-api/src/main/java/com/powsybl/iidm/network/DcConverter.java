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
 * todo ? do we need converter DC nodes polarities ? (pos/neg/middle)
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcConverter<I extends DcConverter<I>> extends Connectable<I>, DcConnectable<I> {

    enum ControlMode {
        P_PCC,
        V_DC
    }

    Terminal getTerminal1();

    Optional<Terminal> getTerminal2();

    /**
     * Get the side the terminal is connected to.
     */
    TwoSides getSide(Terminal terminal);

    Terminal getTerminal(TwoSides side);

    DcTerminal getDcTerminal1();

    DcTerminal getDcTerminal2();

    /**
     * Get the side the DC terminal is connected to.
     */
    TwoSides getSide(DcTerminal dcTerminal);

    DcTerminal getDcTerminal(TwoSides side);

    I setIdleLoss(double idleLoss);

    double getIdleLoss();

    I setSwitchingLoss(double switchingLoss);

    double getSwitchingLoss();

    I setResistiveLoss(double resistiveLoss);

    double getResistiveLoss();

    I setPccTerminal(Terminal pccTerminal);

    Terminal getPccTerminal();

    I setControlMode(ControlMode controlMode);

    ControlMode getControlMode();

    I setTargetP(double targetP);

    double getTargetP();

    I setTargetVdc(double targetVdc);

    double getTargetVdc();
}
