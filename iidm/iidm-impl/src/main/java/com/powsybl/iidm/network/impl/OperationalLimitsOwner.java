/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.Validable;

import java.util.Optional;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface OperationalLimitsOwner extends Validable {

    void remove(LimitType type);

    Optional<String> getActiveLimitId(LimitType limitType);

    void setActiveLimitId(LimitType limitType, String id);

    <L extends AbstractOperationalLimits<L>> void setOperationalLimits(LimitType limitType, L operationalLimits);

    void notifyUpdate(LimitType limitType, String attribute, double oldValue, double newValue);
}
