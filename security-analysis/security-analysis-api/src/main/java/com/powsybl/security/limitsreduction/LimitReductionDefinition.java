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
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;
import com.powsybl.iidm.network.LimitType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LimitReductionDefinition {
    private float limitReduction = 1f;
    private final LimitType limitType;
    private List<ContingencyContext> contingencyContexts = Collections.emptyList();
    private List<NetworkElementCriterion> networkElementCriteria = Collections.emptyList();
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

    public float getLimitReduction() {
        return limitReduction;
    }

    public LimitReductionDefinition setLimitReduction(float limitReduction) {
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
        this.contingencyContexts = Objects.requireNonNull(contingencyContexts);
        return this;
    }

    public List<NetworkElementCriterion> getNetworkElementCriteria() {
        return Collections.unmodifiableList(networkElementCriteria);
    }

    public LimitReductionDefinition setNetworkElementCriteria(NetworkElementCriterion... criteria) {
        return setNetworkElementCriteria(List.of(criteria));
    }

    public LimitReductionDefinition setNetworkElementCriteria(List<NetworkElementCriterion> criteria) {
        this.networkElementCriteria = Objects.requireNonNull(criteria);
        return this;
    }

    public List<LimitDurationCriterion> getDurationCriteria() {
        return Collections.unmodifiableList(durationCriteria);
    }

    public LimitReductionDefinition setDurationCriteria(LimitDurationCriterion... durationCriteria) {
        return setDurationCriteria(List.of(durationCriteria));
    }

    public LimitReductionDefinition setDurationCriteria(List<LimitDurationCriterion> durationCriteria) {
        this.durationCriteria = Objects.requireNonNull(durationCriteria);
        return this;
    }
}
