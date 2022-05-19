/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface OperationalLimitsAdder<L extends OperationalLimits, A extends OperationalLimitsAdder<L, A>> {

    /**
     * Set optional ID. If there are several limits (strictly more than one) in a {@link LoadingLimitsSet}, IDs are mandatory.
     */
    A setId(String id);

    L add();
}
