/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public interface IdentifiableShortCircuitAdder<I extends Identifiable<I>> extends ExtensionAdder<I, IdentifiableShortCircuit<I>> {

    @Override
    default Class<IdentifiableShortCircuit> getExtensionClass() {
        return IdentifiableShortCircuit.class;
    }

    IdentifiableShortCircuitAdder<I> withIpMin(double ipMin);

    IdentifiableShortCircuitAdder<I> withIpMax(double ipMax);

}
