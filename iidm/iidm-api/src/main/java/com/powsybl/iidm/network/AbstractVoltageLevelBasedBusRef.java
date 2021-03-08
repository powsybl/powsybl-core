/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public abstract class AbstractVoltageLevelBasedBusRef implements BusRef {

    protected final VoltageLevel voltageLevel;

    public AbstractVoltageLevelBasedBusRef(VoltageLevel voltageLevel) {
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
    }

}
