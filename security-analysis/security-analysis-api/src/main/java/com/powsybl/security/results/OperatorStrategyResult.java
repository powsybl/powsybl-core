/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.results;

import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class OperatorStrategyResult {

    private final OperatorStrategy operatorStrategy;

    private final LimitViolationsResult limitViolationsResult;

    private final NetworkResult networkResult;

    public OperatorStrategyResult(OperatorStrategy operatorStrategy, LimitViolationsResult limitViolationsResult, NetworkResult networkResult) {
        this.operatorStrategy = Objects.requireNonNull(operatorStrategy);
        this.limitViolationsResult = Objects.requireNonNull(limitViolationsResult);
        this.networkResult = Objects.requireNonNull(networkResult);
    }

    public OperatorStrategy getOperatorStrategy() {
        return operatorStrategy;
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public NetworkResult getNetworkResult() {
        return networkResult;
    }
}
