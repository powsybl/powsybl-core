/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.OperationalLimits;

import java.util.Objects;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractOperationalLimits implements OperationalLimits {

    protected final OperationalLimitsGroupImpl group;

    AbstractOperationalLimits(OperationalLimitsGroupImpl group) {
        this.group = Objects.requireNonNull(group);
    }
}
