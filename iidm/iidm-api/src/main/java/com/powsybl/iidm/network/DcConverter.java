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
 * todo:
 *   converter PccTerminal (as TerminalRef)
 *   converter modes P / Udc - multi variant
 *   converter set-point P (MW) / Udc (kV) - multi variant
 *
 * todo ? do we need converter DC nodes polarities ? (pos/neg/middle)
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcConverter<I extends DcConverter<I>> extends Connectable<I>, DcConnectable<I> {

    Terminal getTerminal1();

    Optional<Terminal> getTerminal2();

    DcTerminal getDcTerminal1();

    DcTerminal getDcTerminal2();

    I setIdleLoss(double idleLoss);

    double getIdleLoss();

    I setSwitchingLoss(double switchingLoss);

    double getSwitchingLoss();

    I setResistiveLoss(double resistiveLoss);

    double getResistiveLoss();
}
