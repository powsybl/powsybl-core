/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.*;

import java.util.Set;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
final class VoltageLevels {

    static final Set<IdentifiableType> MULTIPLE_TERMINALS_CONNECTABLE_TYPES = Sets.immutableEnumSet(
            IdentifiableType.LINE,
            IdentifiableType.TWO_WINDINGS_TRANSFORMER,
            IdentifiableType.THREE_WINDINGS_TRANSFORMER);

    private VoltageLevels() {
    }

    /**
     * Throw a {@link com.powsybl.commons.PowsyblException} if this voltage level contains at least one {@link Branch} or
     * one {@link ThreeWindingsTransformer} or one {@link HvdcConverterStation}.
     */
    static void checkRemovability(VoltageLevel voltageLevel) {
        Network network = voltageLevel.getNetwork();

        for (Connectable connectable : voltageLevel.getConnectables()) {
            IdentifiableType type = connectable.getType();
            if (MULTIPLE_TERMINALS_CONNECTABLE_TYPES.contains(type)) {
                // Reject lines, 2WT and 3WT
                throw new IllegalStateException("The voltage level '" + voltageLevel.getId() + "' cannot be removed because of a remaining " + type);
            } else if (type == IdentifiableType.HVDC_CONVERTER_STATION && network.getHvdcLine((HvdcConverterStation) connectable) != null) {
                // Reject all converter stations connected to a HVDC line
                throw new IllegalStateException("The voltage level '" + voltageLevel.getId() + "' cannot be removed because of a remaining HVDC line");
            }
        }
    }
}
