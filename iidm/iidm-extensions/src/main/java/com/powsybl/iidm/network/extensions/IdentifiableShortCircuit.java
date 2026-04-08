/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.com>}
 */
public interface IdentifiableShortCircuit<I extends Identifiable<I>> extends Extension<I> {

    String NAME = "identifiableShortCircuit";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Get minimum allowable peak short-circuit current [A]
     */
    double getIpMin();

    /**
     * Set minimum allowable peak short-circuit current [A]
     */
    IdentifiableShortCircuit<I> setIpMin(double ipMin);

    /**
     * Get maximum allowable peak short-circuit current
     */
    double getIpMax();

    /**
     * Set maximum allowable peak short-circuit current
     */
    IdentifiableShortCircuit<I> setIpMax(double ipMax);
}
