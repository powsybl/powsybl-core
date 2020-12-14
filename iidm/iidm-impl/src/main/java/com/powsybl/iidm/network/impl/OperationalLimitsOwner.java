/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.OperationalLimits;
import com.powsybl.iidm.network.Validable;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
interface OperationalLimitsOwner extends Validable {

    void setOperationalLimits(LimitType limitType, OperationalLimits operationalLimits);

    void notifyUpdate(LimitType limitType, String attribute, double oldValue, double newValue);
}
