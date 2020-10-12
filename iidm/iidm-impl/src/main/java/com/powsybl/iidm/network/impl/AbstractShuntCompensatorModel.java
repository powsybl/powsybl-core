/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ShuntCompensatorModel;
import com.powsybl.iidm.network.ShuntCompensatorModelType;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractShuntCompensatorModel implements ShuntCompensatorModel {

    protected ShuntCompensatorImpl shuntCompensator;

    public void setShuntCompensator(ShuntCompensatorImpl shuntCompensator) {
        this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
    }

    public abstract ShuntCompensatorModelType getType();

    public abstract int getMaximumSectionCount();

    public abstract double getB(int sectionCount);

    public abstract double getG(int sectionCount);
}
