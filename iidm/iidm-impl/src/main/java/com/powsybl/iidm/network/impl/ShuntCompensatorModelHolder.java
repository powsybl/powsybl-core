/**
 * Copyright (c) 2019, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ShuntCompensatorModel;
import com.powsybl.iidm.network.ShuntCompensatorModelType;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
interface ShuntCompensatorModelHolder extends ShuntCompensatorModel {

    ShuntCompensatorModelType getType();

    void checkCurrentSection(int currentSectionCount);

    void checkCurrentSection(Validable validable, int currentSectionCount);

    void setShuntCompensator(ShuntCompensatorImpl shuntCompensator);
}
