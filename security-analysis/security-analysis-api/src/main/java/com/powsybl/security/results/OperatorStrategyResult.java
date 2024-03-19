/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.results;

import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategyResult {

    public static class ConditionalActionsResult {

        private final String conditionalActionsId;
        private final PostContingencyComputationStatus status;

        private final LimitViolationsResult limitViolationsResult;

        private final NetworkResult networkResult;

        public ConditionalActionsResult(String conditionalActionsId, PostContingencyComputationStatus status, LimitViolationsResult limitViolationsResult,
                           NetworkResult networkResult) {
            this.conditionalActionsId = conditionalActionsId;
            this.status = Objects.requireNonNull(status);
            this.limitViolationsResult = Objects.requireNonNull(limitViolationsResult);
            this.networkResult = Objects.requireNonNull(networkResult);
        }

        public String getConditionalActionsId() {
            return conditionalActionsId;
        }

        public PostContingencyComputationStatus getStatus() {
            return status;
        }

        public LimitViolationsResult getLimitViolationsResult() {
            return limitViolationsResult;
        }

        public NetworkResult getNetworkResult() {
            return networkResult;
        }
    }

    private final OperatorStrategy operatorStrategy;

    private List<ConditionalActionsResult> conditionalActionsResults = new ArrayList<>();

    public OperatorStrategyResult(OperatorStrategy operatorStrategy, PostContingencyComputationStatus status, LimitViolationsResult limitViolationsResult,
                                  NetworkResult networkResult) {
        this.operatorStrategy = Objects.requireNonNull(operatorStrategy);
        this.conditionalActionsResults.add(new ConditionalActionsResult(operatorStrategy.getId(), status, limitViolationsResult, networkResult));
    }

    public OperatorStrategyResult(OperatorStrategy operatorStrategy, List<ConditionalActionsResult> conditionalActionsResults) {
        this.operatorStrategy = Objects.requireNonNull(operatorStrategy);
        this.conditionalActionsResults = conditionalActionsResults;
    }

    public OperatorStrategy getOperatorStrategy() {
        return operatorStrategy;
    }

    public List<ConditionalActionsResult> getConditionalActionsResult() {
        return conditionalActionsResults;
    }
}
