/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.security.LimitViolationsResult;

import java.util.Objects;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractContingencyResult {
    private final LimitViolationsResult limitViolationsResult;
    private final NetworkResult networkResult;

    protected AbstractContingencyResult(LimitViolationsResult limitViolationsResult, NetworkResult networkResult) {
        this.limitViolationsResult = limitViolationsResult;
        this.networkResult = Objects.requireNonNull(networkResult);
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public NetworkResult getNetworkResult() {
        return networkResult;
    }
}
