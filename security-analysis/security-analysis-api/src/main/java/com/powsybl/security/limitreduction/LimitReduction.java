/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

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
public class LimitReduction {
    private final LimitType limitType;
    private final double value;
    private final boolean monitoringOnly;
    private final ContingencyContext contingencyContext;
    private final List<NetworkElementCriterion> networkElementCriteria;
    private final List<LimitDurationCriterion> durationCriteria;

    private boolean isSupportedLimitType(LimitType limitType) {
        return limitType == LimitType.CURRENT
                || limitType == LimitType.ACTIVE_POWER
                || limitType == LimitType.APPARENT_POWER;
    }

    public LimitReduction(LimitType limitType, double value, boolean monitoringOnly) {
        this(limitType, value, monitoringOnly, ContingencyContext.all(), Collections.emptyList(), Collections.emptyList());
    }

    public LimitReduction(LimitType limitType, double value, boolean monitoringOnly,
                          ContingencyContext contingencyContext,
                          List<NetworkElementCriterion> networkElementCriteria,
                          List<LimitDurationCriterion> limitDurationCriteria) {
        if (isSupportedLimitType(limitType)) {
            this.limitType = limitType;
        } else {
            throw new PowsyblException(limitType + " is not a supported limit type for limit reduction");
        }
        if (value > 1. || value < 0.) {
            throw new PowsyblException("Limit reduction value should be in [0;1]");
        }
        this.value = value;
        this.monitoringOnly = monitoringOnly;
        this.contingencyContext = Objects.requireNonNull(contingencyContext);
        this.networkElementCriteria = ImmutableList.copyOf(Objects.requireNonNull(networkElementCriteria));
        this.durationCriteria = ImmutableList.copyOf(Objects.requireNonNull(limitDurationCriteria));
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public double getValue() {
        return value;
    }

    public boolean isMonitoringOnly() {
        return monitoringOnly;
    }

    public ContingencyContext getContingencyContext() {
        return contingencyContext;
    }

    public List<NetworkElementCriterion> getNetworkElementCriteria() {
        return networkElementCriteria;
    }

    public List<LimitDurationCriterion> getDurationCriteria() {
        return durationCriteria;
    }
}
