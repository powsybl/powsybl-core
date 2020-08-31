/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractShuntCompensatorModel implements ShuntCompensatorModelWrapper {

    protected ShuntCompensatorImpl shuntCompensator;

    @Override
    public void setShuntCompensator(ShuntCompensatorImpl shuntCompensator) {
        if (this.shuntCompensator != null) {
            throw new PowsyblException("Owner (shunt compensator) already defined");
        }
        this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
    }
}
