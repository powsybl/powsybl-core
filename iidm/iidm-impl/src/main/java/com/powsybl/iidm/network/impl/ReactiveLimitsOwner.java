/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveLimits;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
interface ReactiveLimitsOwner {

    void setReactiveLimits(ReactiveLimits reactiveLimits);
}
