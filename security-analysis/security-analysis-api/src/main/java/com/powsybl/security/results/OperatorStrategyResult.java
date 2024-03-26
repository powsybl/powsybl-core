/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.commons.PowsyblException;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.*;

/**
 * An operator strategy could contain several conditional actions (also called stages). Each stage can be monitored through
 * a computation status, limit violations and network results. Nevertheless, an operator strategy result always contains the
 * final stage computation status, the limit violations on the final state of the network and the network result of the final state.
 *
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

    /**
     * @return The PostContingencyComputationStatus of the operator strategy. In case of multiple stage, it represents the
     * status after applying the last conditional actions.
     */
    public PostContingencyComputationStatus getStatus() {
        return getFinalOperatorStrategyResult().getStatus();
    }

    /**
     * @return The LimitViolationsResult of the operator strategy. In case of multiple stage, it represents the limit
     * violations monitored on the network state after applying the last conditional actions.
     */
    public LimitViolationsResult getLimitViolationsResult() {
        return getFinalOperatorStrategyResult().getLimitViolationsResult();
    }

    /**
     * @return The NetworkResult of the operator strategy. In case of multiple stage, it represents the network
     * result monitored on the network state after applying the last conditional actions.
     */
    public NetworkResult getNetworkResult() {
        return getFinalOperatorStrategyResult().getNetworkResult();
    }

    /**
     * @return The Operator strategy associated to this result
     */
    public OperatorStrategy getOperatorStrategy() {
        return operatorStrategy;
    }

    /**
     * @return The list of all ConditionalActionsResult of the associated operator strategy
     */
    public List<ConditionalActionsResult> getConditionalActionsResults() {
        return conditionalActionsResults;
    }

    /**
     * @return The conditional actions result associated to the last stage of the strategy.
     */
    public ConditionalActionsResult getFinalOperatorStrategyResult() {
        if (!conditionalActionsResults.isEmpty()) {
            return conditionalActionsResults.get(conditionalActionsResults.size() - 1);
        } else {
            throw new PowsyblException("No conditional action results available.");
        }
    }
}
