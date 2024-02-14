/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitsreduction;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.security.limitsreduction.criterion.duration.LimitDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.network.AbstractNetworkElementCriterion;

import java.util.*;

/**
 * Contains the definitions of the applied limit reductions.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class LimitReductionDefinitionList {

    private List<LimitReductionDefinition> limitReductionDefinitions = Collections.emptyList();

    public static class LimitReductionDefinition {
        private double limitReduction = 1d;
        private final LimitType limitType;
        private List<ContingencyContext> contingencyContexts = Collections.emptyList();
        private List<AbstractNetworkElementCriterion> networkElementCriteria = Collections.emptyList();
        private List<LimitDurationCriterion> durationCriteria = Collections.emptyList();

        private boolean isSupportedLimitType(LimitType limitType) {
            return limitType == LimitType.CURRENT
                    || limitType == LimitType.ACTIVE_POWER
                    || limitType == LimitType.APPARENT_POWER;
        }

        public LimitReductionDefinition(LimitType limitType) {
            if (isSupportedLimitType(limitType)) {
                this.limitType = limitType;
            } else {
                throw new PowsyblException(limitType + " is not a supported limit type for limit reduction");
            }
        }

        public LimitType getLimitType() {
            return limitType;
        }

        public double getLimitReduction() {
            return limitReduction;
        }

        public LimitReductionDefinition setLimitReduction(double limitReduction) {
            if (limitReduction > 1. || limitReduction < 0.) {
                throw new PowsyblException("Limit reduction value should be in [0;1]");
            }
            this.limitReduction = limitReduction;
            return this;
        }

        public List<ContingencyContext> getContingencyContexts() {
            return Collections.unmodifiableList(contingencyContexts);
        }

        public LimitReductionDefinition setContingencyContexts(ContingencyContext... contingencyContexts) {
            return setContingencyContexts(List.of(contingencyContexts));
        }

        public LimitReductionDefinition setContingencyContexts(List<ContingencyContext> contingencyContexts) {
            this.contingencyContexts = contingencyContexts;
            return this;
        }

        public List<AbstractNetworkElementCriterion> getNetworkElementCriteria() {
            return Collections.unmodifiableList(networkElementCriteria);
        }

        public LimitReductionDefinition setNetworkElementCriteria(AbstractNetworkElementCriterion... criteria) {
            return setNetworkElementCriteria(List.of(criteria));
        }

        public LimitReductionDefinition setNetworkElementCriteria(List<AbstractNetworkElementCriterion> criteria) {
            this.networkElementCriteria = criteria;
            return this;
        }

        public List<LimitDurationCriterion> getDurationCriteria() {
            return Collections.unmodifiableList(durationCriteria);
        }

        public LimitReductionDefinition setDurationCriteria(LimitDurationCriterion... durationCriteria) {
            return setDurationCriteria(List.of(durationCriteria));
        }

        public LimitReductionDefinition setDurationCriteria(List<LimitDurationCriterion> durationCriteria) {
            this.durationCriteria = durationCriteria;
            return this;
        }
    }

    public List<LimitReductionDefinition> getLimitReductionDefinitions() {
        return Collections.unmodifiableList(limitReductionDefinitions);
    }

    public LimitReductionDefinitionList setLimitReductionDefinitions(LimitReductionDefinition... definitions) {
        return setLimitReductionDefinitions(List.of(definitions));
    }

    public LimitReductionDefinitionList setLimitReductionDefinitions(List<LimitReductionDefinition> definitions) {
        limitReductionDefinitions = definitions;
        return this;
    }
}
