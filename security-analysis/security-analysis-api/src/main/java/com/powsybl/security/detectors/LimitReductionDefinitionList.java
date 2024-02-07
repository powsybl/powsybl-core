/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.security.detectors.criterion.duration.LimitDurationCriterion;
import com.powsybl.security.detectors.criterion.network.AbstractNetworkElementCriterion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains the definitions of the applied limit reductions.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class LimitReductionDefinitionList {

    private List<LimitReductionDefinition> limitReductionDefinitions = new ArrayList<>();

    public static class LimitReductionDefinition {
        private double limitReduction = 1d;
        private LimitType limitType;
        private List<ContingencyContext> contingencyContexts = List.of(ContingencyContext.all());
        private List<AbstractNetworkElementCriterion> networkElementCriteria = new ArrayList<>();
        private List<LimitDurationCriterion> durationCriteria = new ArrayList<>();

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
            this.limitReduction = limitReduction;
            return this;
        }

        public List<ContingencyContext> getContingencyContexts() {
            return contingencyContexts;
        }

        public LimitReductionDefinition setContingencyContexts(List<ContingencyContext> contingencyContexts) {
            this.contingencyContexts = contingencyContexts;
            return this;
        }

        public List<AbstractNetworkElementCriterion> getNetworkElementCriteria() {
            return networkElementCriteria;
        }

        public LimitReductionDefinition setNetworkElementCriteria(List<AbstractNetworkElementCriterion> criteria) {
            this.networkElementCriteria = criteria;
            return this;
        }

        public List<LimitDurationCriterion> getDurationCriteria() {
            return durationCriteria;
        }

        public LimitReductionDefinition setDurationCriteria(List<LimitDurationCriterion> durationCriteria) {
            this.durationCriteria = durationCriteria;
            return this;
        }
    }

    public List<LimitReductionDefinition> getLimitReductionDefinitions() {
        return Collections.unmodifiableList(limitReductionDefinitions);
    }
}
