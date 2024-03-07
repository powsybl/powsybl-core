/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitsreduction;

import com.google.common.collect.ImmutableList;
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
    private final LimitType limitType;
    private final float limitReduction;
    private final List<ContingencyContext> contingencyContexts;
    private final List<NetworkElementCriterion> networkElementCriteria;
    private final List<LimitDurationCriterion> durationCriteria;

    private boolean isSupportedLimitType(LimitType limitType) {
        return limitType == LimitType.CURRENT
                || limitType == LimitType.ACTIVE_POWER
                || limitType == LimitType.APPARENT_POWER;
    }

    public LimitReductionDefinition(LimitType limitType, float limitReduction) {
        this(limitType, limitReduction, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public LimitReductionDefinition(LimitType limitType, float limitReduction,
                                    List<ContingencyContext> contingencyContexts,
                                    List<NetworkElementCriterion> networkElementCriteria,
                                    List<LimitDurationCriterion> limitDurationCriteria) {
        if (isSupportedLimitType(limitType)) {
            this.limitType = limitType;
        } else {
            throw new PowsyblException(limitType + " is not a supported limit type for limit reduction");
        }
        if (limitReduction > 1. || limitReduction < 0.) {
            throw new PowsyblException("Limit reduction value should be in [0;1]");
        }
        this.limitReduction = limitReduction;
        this.contingencyContexts = ImmutableList.copyOf(Objects.requireNonNull(contingencyContexts));
        this.networkElementCriteria = ImmutableList.copyOf(Objects.requireNonNull(networkElementCriteria));
        this.durationCriteria = ImmutableList.copyOf(Objects.requireNonNull(limitDurationCriteria));
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public float getLimitReduction() {
        return limitReduction;
    }

    public List<ContingencyContext> getContingencyContexts() {
        return contingencyContexts;
    }

    public List<NetworkElementCriterion> getNetworkElementCriteria() {
        return networkElementCriteria;
    }

    public List<LimitDurationCriterion> getDurationCriteria() {
        return durationCriteria;
    }
}
