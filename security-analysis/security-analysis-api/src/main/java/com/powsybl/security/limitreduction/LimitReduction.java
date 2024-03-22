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
 * <p>This class represents a reduction that should be applied to limits of a certain type.</p>
 * <p>A reduced limit is computed as <code>original limit * limitReduction.value</code>.</p>
 * <p>It may also contain restrictions indicating in which conditions it should be applied. If no restrictions are defined
 * the limit reduction will apply to all limits of the defined type.</p>
 * <p>The possible restrictions are:
 *     <ul>
 *         <li><code>monitoringOnly</code>: Does the restriction applies for monitoring use case only (<code>true</code>),
 *              or for both monitoring and action use cases (<code>false</code>)?</li>
 *         <li><code>contingencyContext</code>: The contingency context for which the limit reduction applies (in pre-contingency only, after every contingency, ...);</li>
 *         <li><code>networkElementCriteria</code>: Criteria that a network element should respect for the limit reduction to apply on its limits;</li>
 *         <li><code>limitDurationCriteria</code>: Criteria on a duration aspect that a limit should respect for the limit reduction to apply on it
 *              (permanent limit, temporary limits with an acceptable duration defined within a specific range, ...).</li>
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
     * <p>Create a limit reduction applying on each limits of a given type.</p>
     * <p>This reduction will apply for every contingency context, and for monitoring and action use cases.</p>
     *
     * @param limitType the type of the limits to reduce
     * @param value the value of the reduction (reduced limits will be equal to <code>original limit * reduction value</code>).
     */
    public LimitReduction(LimitType limitType, double value) {
        this(limitType, value, false);
    }

    /**
     * <p>Create a limit reduction applying on each limits of a given type, for monitoring only or monitoring/action
     * use cases depending on the <code>monitoringOnly</code> parameter.</p>
     * <p>This reduction will apply for every contingency context.</p>
     *
     * @param limitType the type of the limits to reduce
     * @param value the value of the reduction (reduced limits will be equal to <code>original limit * reduction value</code>).
     * @param monitoringOnly <code>true</code> if the reduction should apply only for monitoring use case, <code>false</code> otherwise.
     */
    public LimitReduction(LimitType limitType, double value, boolean monitoringOnly) {
        this(limitType, value, monitoringOnly, ContingencyContext.all(), Collections.emptyList(), Collections.emptyList());
    }

    /**
     * <p>Initiate a builder for creating more specific limit reductions (indicate a contingency context or criteria
     * on network elements or limit durations).</p>
     *
     * @param limitType the type of the limits to reduce
     * @param value the value of the reduction (reduced limits will be equal to <code>original limit * reduction value</code>).
     * @return a builder to use to create a {@link LimitReduction}.
     */
    public static LimitReduction.Builder builder(LimitType limitType, double value) {
        return new Builder(limitType, value);
    }

    /**
     * <p>Builder used to create a {@link LimitReduction}.</p>
     * <p>The default values for the {@link LimitReduction} to create are the following:
     *     <ul>
     *         <li><code>monitoringOnly</code>: <code>false</code>. The limit reduction will apply for all use cases (monitoring and action);</li>
     *         <li><code>contingencyContext</code>: {@link ContingencyContext#all()}. The limit reduction will apply in pre-contingency and after every contingency.</li>
     *         <li><code>networkElementCriteria</code>: {@link Collections#emptyList()}. The limit reduction will apply for every network element.</li>
     *         <li><code>limitDurationCriteria</code>: {@link Collections#emptyList()}. The limit reduction will apply for all permanent and temporary limits.</li>
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
         * <p>Define for which use cases the limit reduction should apply.</p>
         * <p>By default, the limit reduction will apply for all use cases (monitoring and action).</p>
         *
         * @param monitoringOnly <code>true</code> if the limit reduction should only apply for monitoring use case, <code>false</code> otherwise.
         * @return the current {@link Builder}
         */
        public Builder withMonitoringOnly(boolean monitoringOnly) {
            this.monitoringOnly = monitoringOnly;
            return this;
        }

        /**
         * <p>Define for which contingency context the limit reduction should apply.</p>
         * <p>By default, the limit reduction will apply in pre-contingency and after every contingency.</p>
         *
         * @param contingencyContext the contingency context for which contingency context the limit reduction should apply.
         *                           It could not be null.
         * @return the current {@link Builder}
         */
        public Builder withContingencyContext(ContingencyContext contingencyContext) {
            this.contingencyContext = Objects.requireNonNull(contingencyContext);
            return this;
        }

        /**
         * <p>Define criteria to use to define the network elements for which the limit reduction should apply.</p>
         * <p>By default, the limit reduction will apply for every network element.</p>
         * <p>This method replace all the previously network element criteria by the given ones.</p>
         *
         * @param networkElementCriteria criteria to use to restrict the limit reduction to certain network elements.
         * @return the current {@link Builder}
         */
        public Builder withNetworkElementCriteria(NetworkElementCriterion... networkElementCriteria) {
            return withNetworkElementCriteria(List.of(networkElementCriteria));
        }

        /**
         * <p>Define criteria to use to define the network elements for which the limit reduction should apply.</p>
         * <p>By default, the limit reduction will apply for every network element.</p>
         * <p>This method replace all the previously network element criteria by the given ones.</p>
         *
         * @param networkElementCriteria criteria to use to restrict the limit reduction to certain network elements.
         * @return the current {@link Builder}
         */
        public Builder withNetworkElementCriteria(List<NetworkElementCriterion> networkElementCriteria) {
            this.networkElementCriteria = ImmutableList.copyOf(Objects.requireNonNull(networkElementCriteria));
            return this;
        }

        /**
         * <p>Define criteria to use to define if the limit reduction should apply on permanent limits,
         * temporary limits (with possible restrictions) or both.</p>
         * <p>By default, the limit reduction will apply for all permanent and temporary limits.</p>
         * <p>This method replace all the previously limit duration criteria by the given ones.</p>
         *
         * @param limitDurationCriteria criteria to use to restrict the limit reduction to certain durations.
         * @return the current {@link Builder}
         */
        public Builder withLimitDurationCriteria(LimitDurationCriterion... limitDurationCriteria) {
            return withLimitDurationCriteria(List.of(limitDurationCriteria));
        }

        /**
         * <p>Define criteria to use to define if the limit reduction should apply on permanent limits,
         * temporary limits (with possible restrictions) or both.</p>
         * <p>By default, the limit reduction will apply for all permanent and temporary limits.</p>
         * <p>This method replace all the previously limit duration criteria by the given ones.</p>
         *
         * @param limitDurationCriteria criteria to use to restrict the limit reduction to certain durations.
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
     * <p>Indicate if the limit reduction applies only for monitoring use case (<code>true</code>),
     * or for monitoring and action use cases (<code>false</code>).</p>
     *
     * @return <code>true</code> if the limit reduction applies only for monitoring use case, <code>false</code> otherwise.
     */
    public boolean isMonitoringOnly() {
        return monitoringOnly;
    }

    /**
     * <p>Indicate for which contingency context the limit reduction applies.</p>
     *
     * @return the {@link ContingencyContext} of the limit reduction.
     */
    public ContingencyContext getContingencyContext() {
        return contingencyContext;
    }

    /**
     * <p>Indicate the criteria used to define the network elements for which the limit reduction applies.</p>
     *
     * @return the list of the {@link NetworkElementCriterion} of the limit reduction.
     */
    public List<NetworkElementCriterion> getNetworkElementCriteria() {
        return networkElementCriteria;
    }

    /**
     * <p>Indicate criteria used to restrict the limit reduction to certain durations.</p>
     *
     * @return the list of the {@link LimitDurationCriterion} of the limit reduction.
     */
    public List<LimitDurationCriterion> getDurationCriteria() {
        return durationCriteria;
    }
}
