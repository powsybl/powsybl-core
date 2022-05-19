/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface OperationalLimitsSet<L extends OperationalLimits> {

    L getLimits(String id);

    Optional<L> getActiveLimits();

    Collection<L> getLimits();

    void remove();
}
