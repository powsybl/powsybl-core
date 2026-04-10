/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Set;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface ObservabilityAreaAdder extends ExtensionAdder<VoltageLevel, ObservabilityArea> {

    @Override
    default Class<? super ObservabilityArea> getExtensionClass() {
        return ObservabilityArea.class;
    }

    /**
     * Set observability area by electrical bus. Must be used ONLY if the observability areas are consistent with the current topology.
     * If the observability areas are NOT consistent with the current topology, use {@link ObservabilityAreaAdder#withObservabilityAreaByNodes}
     * or {@link ObservabilityAreaAdder#withObservabilityAreaByBusBreakerViewBuses}.
     */
    ObservabilityAreaAdder withObservabilityAreaByBusViewBus(String busViewBusId, int observabilityAreaNumber,
                                                             ObservabilityArea.ObservabilityStatus status);

    ObservabilityAreaAdder withObservabilityAreaByBusBreakerViewBuses(Set<String> busBreakerViewBusIds, int observabilityAreaNumber,
                                                             ObservabilityArea.ObservabilityStatus status);

    ObservabilityAreaAdder withObservabilityAreaByNodes(Set<Integer> nodes, int observabilityAreaNumber, ObservabilityArea.ObservabilityStatus status);
}
