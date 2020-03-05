/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ShuntCompensatorModel;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractShuntCompensatorModel implements ShuntCompensatorModelHolder {

    protected ShuntCompensatorImpl shuntCompensator;

    @Override
    public void setShuntCompensator(ShuntCompensatorImpl shuntCompensator) {
        this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
    }

    @Override
    public ShuntCompensatorModel getModel() {
        return this;
    }

    @Override
    public <M extends ShuntCompensatorModel> M getModel(Class<M> modelType) {
        if (modelType == null) {
            throw new IllegalArgumentException("shunt compensator model type is null");
        }
        if (modelType.isInstance(this)) {
            return modelType.cast(this);
        }
        throw new ValidationException(shuntCompensator, "incorrect shunt compensator model type " +
                modelType.getName() + ", expected " + getClass());
    }

    void checkCurrentSection(int currentSectionCount) {
        checkCurrentSection(shuntCompensator, currentSectionCount);
    }

    abstract void checkCurrentSection(Validable validable, int currentSectionCount);
}
