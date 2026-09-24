/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.converter;

import com.powsybl.action.ActionList;
import com.powsybl.contingency.list.ContingencyList;
import com.powsybl.contingency.strategy.OperatorStrategyList;
import com.powsybl.security.monitor.StateMonitor;

import java.util.List;
import java.util.Objects;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public record NcConversionResult(ContingencyList contingencyList, List<StateMonitor> stateMonitors,
                                 ActionList actionList, OperatorStrategyList operatorStrategyList) {
    public NcConversionResult {
        Objects.requireNonNull(contingencyList);
        stateMonitors = List.copyOf(Objects.requireNonNull(stateMonitors));
        Objects.requireNonNull(actionList);
        Objects.requireNonNull(operatorStrategyList);
    }
}
