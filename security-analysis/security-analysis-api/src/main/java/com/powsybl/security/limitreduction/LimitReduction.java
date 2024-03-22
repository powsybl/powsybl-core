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
 * <p>This class represents a reduction that should be applied to operational limits of a certain type.</p>
 * <p>A reduced limit is computed as <code>original limit * limitReduction.value</code>.</p>
 * <p>It may also contain restrictions indicating in which conditions it should be applied. If no restriction is defined
 * the limit reduction is applied to all limits of the defined type.</p>
 * <p>The possible restrictions are:
 *     <ul>
 *         <li><code>monitoringOnly</code>: use <code>true</code> if the limit reduction is applied when reporting the limit violations only.
 *              Use <code>false</code> if it is applied also inside the conditions of operator strategies. The default value is <code>false</code>.</li>
 *         <li><code>contingencyContext</code>: the contingency context in which the limit reduction is applied (in pre-contingency only, after every contingency, etc.);</li>
 *         <li><code>networkElementCriteria</code>: criteria a network element should respect for the limit reduction to be applied on its limits;</li>
 *         <li><code>limitDurationCriteria</code>: criteria based on limit overload acceptable durations. Through these criteria, we can defined if
 *         the reduction is applied on the permanent limit and/or on a temporary limit if it acceptable duration is within a specific range.</li>
 *     </ul>
 * </p>
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

    /**
     * <p>Create a limit reduction applying on each operational limits of a given type.</p>
     * <p>This reduction is applied for a contingency context ALL and for limit violations reporting and operator strategy conditions.</p>
     *
     * @param limitType the type of the limits to reduce.
     * @param value the value of the reduction (reduced limits are equal to <code>original limit * reduction value</code>).
     */
    public LimitReduction(LimitType limitType, double value) {
        this(limitType, value, false);
    }

    /**
     * <p>Create a limit reduction applying on each limits of a given type, for monitoring only or monitoring/action
     * depending on the <code>monitoringOnly</code> parameter.</p>
     * <p>This reduction is applied for a contingency context ALL.</p>
     *
     * @param limitType the type of the limits to reduce
     * @param value the value of the reduction (reduced limits are equal to <code>original limit * reduction value</code>).
     * @param monitoringOnly <code>true</code> if the reduction is applied only for monitoring only, <code>false</code> otherwise.
     */
    public LimitReduction(LimitType limitType, double value, boolean monitoringOnly) {
        this(limitType, value, monitoringOnly, ContingencyContext.all(), Collections.emptyList(), Collections.emptyList());
    }

    /**
     * <p>Initialize a builder for creating more specific limit reductions (indicate a contingency context or criteria
     * on network elements or on limit durations).</p>
     *
     * @param limitType the type of the limits to reduce.
     * @param value the value of the reduction (reduced limits are equal to <code>original limit * reduction value</code>).
     * @return a builder used to create a {@link LimitReduction}.
     */
    public static LimitReduction.Builder builder(LimitType limitType, double value) {
        return new Builder(limitType, value);
    }

    /**
     * <p>Builder used to create a {@link LimitReduction}.</p>
     * <p>The default values for the {@link LimitReduction} are the following:
     *     <ul>
     *         <li><code>monitoringOnly</code>: <code>false</code>. The limit reduction is applied for monitoring limit violations and conditions of operator strategies;</li>
     *         <li><code>contingencyContext</code>: {@link ContingencyContext#all()}. The limit reduction is used on pre-contingency state and after each contingency state.</li>
     *         <li><code>networkElementCriteria</code>: {@link Collections#emptyList()}. The limit reduction is applied on each network element (that holds a limit on this type).</li>
     *         <li><code>limitDurationCriteria</code>: {@link Collections#emptyList()}. The limit reduction is applied for all permanent and temporary limits.</li>
     *     </ul>
     * </p>
     */
    public static class Builder {
        private final LimitType limitType;
        private final double value;
        private boolean monitoringOnly = false;
        private ContingencyContext contingencyContext = ContingencyContext.all();
        private List<NetworkElementCriterion> networkElementCriteria = Collections.emptyList();
        private List<LimitDurationCriterion> limitDurationCriteria = Collections.emptyList();

        protected Builder(LimitType limitType, double value) {
            this.limitType = limitType;
            this.value = value;
        }

        /**
         * <p>Define if the limit reduction is applied only for limit violations report or also inside conditions of operator strategies.</p>
         * <p>By default, the limit reduction is apply for both steps.</p>
         *
         * @param monitoringOnly <code>true</code> if the limit reduction is applied for monitoring only, <code>false</code> otherwise.
         * @return the current {@link Builder}
         */
        public Builder withMonitoringOnly(boolean monitoringOnly) {
            this.monitoringOnly = monitoringOnly;
            return this;
        }

        /**
         * <p>Define in which contingency context the limit reduction is applied.</p>
         * <p>By default, the limit reduction is used in pre-contingency state and after each contingency state.</p>
         *
         * @param contingencyContext the contingency context of the limit reduction to be applied.
         * @return the current {@link Builder}
         */
        public Builder withContingencyContext(ContingencyContext contingencyContext) {
            this.contingencyContext = Objects.requireNonNull(contingencyContext);
            return this;
        }

        /**
         * <p>Define criteria on network elements.</p>
         * <p>By default, the limit reduction is applied on each network element that holds a limit of the good type.</p>
         * <p>This method is not cumulative and clean previous definitions.</p>
         *
         * @param networkElementCriteria criteria on network elements on which the limit reduction is applied.
         * @return the current {@link Builder}
         */
        public Builder withNetworkElementCriteria(NetworkElementCriterion... networkElementCriteria) {
            return withNetworkElementCriteria(List.of(networkElementCriteria));
        }

        /**
         * <p>Define criteria on network elements.</p>
         * <p>By default, the limit reduction is applied on each network element that holds a limit of the good type.</p>
         * <p>This method is not cumulative and clean previous definitions.</p>
         *
         * @param networkElementCriteria criteria on network elements on which the limit reduction is applied.
         * @return the current {@link Builder}
         */
        public Builder withNetworkElementCriteria(List<NetworkElementCriterion> networkElementCriteria) {
            this.networkElementCriteria = ImmutableList.copyOf(Objects.requireNonNull(networkElementCriteria));
            return this;
        }

        /**
         * <p>Define criteria on permanent limit and/or on acceptable durations of temporary limits within a specific range.</p>
         * <p>By default, the limit reduction is applied for all permanent and temporary limits of the good type.</p>
         * <p>This method is not cumulative and clean previous definitions.</p>
         *
         * @param limitDurationCriteria criteria to restrict the limit reduction to specific durations.
         * @return the current {@link Builder}
         */
        public Builder withLimitDurationCriteria(LimitDurationCriterion... limitDurationCriteria) {
            return withLimitDurationCriteria(List.of(limitDurationCriteria));
        }

        /**
         * <p>Define criteria on permanent limit and/or on acceptable durations of temporary limits within a specific range.</p>
         * <p>By default, the limit reduction is applied for all permanent and temporary limits of the good type.</p>
         * <p>This method is not cumulative and clean previous definitions.</p>
         *
         * @param limitDurationCriteria criteria to restrict the limit reduction to specific durations.
         * @return the current {@link Builder}
         */
        public Builder withLimitDurationCriteria(List<LimitDurationCriterion> limitDurationCriteria) {
            this.limitDurationCriteria = ImmutableList.copyOf(Objects.requireNonNull(limitDurationCriteria));
            return this;
        }

        /**
         * <p>Build the {@link LimitReduction} with the defined parameters.</p>
         * @return a new {@link LimitReduction}
         */
        public LimitReduction build() {
            return new LimitReduction(limitType, value, monitoringOnly, contingencyContext,
                    networkElementCriteria, limitDurationCriteria);
        }
    }

    private LimitReduction(LimitType limitType, double value, boolean monitoringOnly,
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
        this.contingencyContext = contingencyContext;
        this.networkElementCriteria = networkElementCriteria;
        this.durationCriteria = limitDurationCriteria;
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public double getValue() {
        return value;
    }

    /**
     * <p>Indicate if the limit reduction is applied only to report limit violations (<code>true</code>),
     * or if also affects the conditions of operator strategies (<code>false</code>).</p>
     *
     * @return <code>true</code> if the limit reduction is applied only for monitoring, <code>false</code> otherwise.
     */
    public boolean isMonitoringOnly() {
        return monitoringOnly;
    }

    /**
     * <p>Indicate the limit reduction contingency context.</p>
     *
     * @return the {@link ContingencyContext} of the limit reduction.
     */
    public ContingencyContext getContingencyContext() {
        return contingencyContext;
    }

    /**
     * <p>Indicate the criteria on network elements candidate for the limit reduction.</p>
     *
     * @return the list of the {@link NetworkElementCriterion} candidate for the limit reduction.
     */
    public List<NetworkElementCriterion> getNetworkElementCriteria() {
        return networkElementCriteria;
    }

    /**
     * <p>Indicate criteria on operational limit acceptable durations.</p>
     *
     * @return the list of the {@link LimitDurationCriterion} of the limit reduction.
     */
    public List<LimitDurationCriterion> getDurationCriteria() {
        return durationCriteria;
    }
}
