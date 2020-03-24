/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
interface ShuntCompensatorModelWrapper extends ShuntCompensatorModel {

    ShuntCompensatorModelType getType();

    void setShuntCompensator(ShuntCompensatorImpl owner);

    boolean containsSection(int sectionNumber);

    int getMaximumSectionCount();
}
