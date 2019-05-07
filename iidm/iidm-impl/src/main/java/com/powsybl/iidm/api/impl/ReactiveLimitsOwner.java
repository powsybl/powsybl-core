/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.api.impl;

import com.powsybl.iidm.api.ReactiveLimits;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
interface ReactiveLimitsOwner {

    void setReactiveLimits(ReactiveLimits reactiveLimits);
}
