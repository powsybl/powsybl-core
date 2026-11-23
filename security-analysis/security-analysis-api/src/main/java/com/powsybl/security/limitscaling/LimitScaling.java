/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling;

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
 * <p>This class represents a scaling that should be applied to operational limits of a certain type.</p>
 * <p>A scaled limit is computed as <code>original limit * limitScaling.value</code>.</p>
 * <p>It may also contain restrictions indicating in which conditions it should be applied. If no restriction is defined,
 * the limit scaling is applied to all limits of the defined type.
 * Note that restrictions are cumulative between themselves (but the Builder's methods to define those lists are not cumulative).</p>
 * <p>The possible restrictions are:
 *     <ul>
 *         <li><code>monitoringOnly</code>: use <code>true</code> if the limit scaling is applied when reporting the limit violations only.
 *              Use <code>false</code> if it is applied also inside the conditions of operator strategies. The default value is <code>false</code>.</li>
 *         <li><code>contingencyContext</code>: the contingency context in which the limit scaling is applied (in pre-contingency only, after every contingency, etc.);</li>
 *         <li><code>networkElementCriteria</code>: criteria a network element should respect for the limit scaling to be applied on its limits;</li>
 *         <li><code>limitDurationCriteria</code>: criteria based on limit overload acceptable durations. Through these criteria, we can define if
 *         the scaling is applied on the permanent limit and/or on a temporary limit and if its acceptable duration is within a specific range.</li>
 *         <li><code>operationalLimitsGroupIdSelection</code>: define which groups the scaling should be applied to by specifying their ID</li>
 *     </ul>
 * </p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LimitScaling {
    private final LimitType limitType;
    private final double value;
    private final boolean monitoringOnly;
    private final ContingencyContext contingencyContext;
    private final List<NetworkElementCriterion> networkElementCriteria;
    private final List<LimitDurationCriterion> durationCriteria;
    private final List<String> operationalLimitsGroupIdsSelection;

    private boolean isSupportedLimitType(LimitType limitType) {
        return limitType == LimitType.CURRENT
                || limitType == LimitType.ACTIVE_POWER
                || limitType == LimitType.APPARENT_POWER;
    }

    /**
     * <p>Create a limit scaling applying on each operational limits of a given type.</p>
     * <p>This scaling is applied for a contingency context ALL and for limit violations reporting and operator strategy conditions.</p>
     *
     * @param limitType the type of the limits to scale.
     * @param value the value of the scaling (scaled limits are equal to <code>original limit * scaling value</code>).
     */
    public LimitScaling(LimitType limitType, double value) {
        this(limitType, value, false);
    }

    /**
     * <p>Create a limit scaling applying on each limits of a given type, for monitoring only or monitoring/action
     * depending on the <code>monitoringOnly</code> parameter.</p>
     * <p>This scaling is applied for a contingency context ALL.</p>
     *
     * @param limitType the type of the limits to scale
     * @param value the value of the scaling (scaled limits are equal to <code>original limit * scaling value</code>).
     * @param monitoringOnly <code>true</code> if the scaling is applied only for monitoring only, <code>false</code> otherwise.
     */
    public LimitScaling(LimitType limitType, double value, boolean monitoringOnly) {
        this(limitType, value, monitoringOnly, ContingencyContext.all(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    private LimitScaling(LimitType limitType, double value, boolean monitoringOnly,
                         ContingencyContext contingencyContext,
                         List<NetworkElementCriterion> networkElementCriteria,
                         List<LimitDurationCriterion> limitDurationCriteria,
                         List<String> operationalLimitsGroupIdsSelection) {
        if (isSupportedLimitType(limitType)) {
            this.limitType = limitType;
        } else {
            throw new PowsyblException(limitType + " is not a supported limit type for limit scaling");
        }
        if (value < 0.) {
            throw new PowsyblException("Limit scaling value should be equal or greater than 0");
        }
        this.value = value;
        this.monitoringOnly = monitoringOnly;
        this.contingencyContext = contingencyContext;
        this.networkElementCriteria = networkElementCriteria;
        this.durationCriteria = limitDurationCriteria;
        this.operationalLimitsGroupIdsSelection = operationalLimitsGroupIdsSelection;
    }

    /**
     * <p>Initialize a builder for creating more specific limit scalings (indicate a contingency context or criteria
     * on network elements or on limit durations).</p>
     *
     * @param limitType the type of the limits to scale.
     * @param value the value of the scaling (scaled limits are equal to <code>original limit * scaling value</code>).
     * @return a builder used to create a {@link LimitScaling}.
     */
    public static LimitScaling.Builder builder(LimitType limitType, double value) {
        return new Builder(limitType, value);
    }

    /**
     * <p>Builder used to create a {@link LimitScaling}.</p>
     * <p>The default values for the {@link LimitScaling} are the following:
     *     <ul>
     *         <li><code>monitoringOnly</code>: <code>false</code>. The limit scaling is applied for monitoring limit violations and conditions of operator strategies;</li>
     *         <li><code>contingencyContext</code>: {@link ContingencyContext#all()}. The limit scaling is used on pre-contingency state and after each contingency state.</li>
     *         <li><code>networkElementCriteria</code>: {@link Collections#emptyList()}. The limit scaling is applied on each network element (that holds a limit on this type).</li>
     *         <li><code>limitDurationCriteria</code>: {@link Collections#emptyList()}. The limit scaling is applied for all permanent and temporary limits.</li>
     *         <li><code>operationalLimitsGroupIdSelection</code>: {@link Collections#emptyList()}. The limit scaling is applied to all selected groups.</li>
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
        private List<String> operationalLimitsGroupIdsSelection = Collections.emptyList();

        protected Builder(LimitType limitType, double value) {
            this.limitType = limitType;
            this.value = value;
        }

        /**
         * <p>Define if the limit scaling is applied only for limit violations report or also inside conditions of operator strategies.</p>
         * <p>By default, the limit scaling is applied for both steps.</p>
         *
         * @param monitoringOnly <code>true</code> if the limit scaling is applied for monitoring only, <code>false</code> otherwise.
         * @return the current {@link Builder}
         */
        public Builder withMonitoringOnly(boolean monitoringOnly) {
            this.monitoringOnly = monitoringOnly;
            return this;
        }

        /**
         * <p>Define in which contingency context the limit scaling is applied.</p>
         * <p>By default, the limit scaling is used in pre-contingency state and after each contingency state.</p>
         *
         * @param contingencyContext the contingency context of the limit scaling to be applied.
         * @return the current {@link Builder}
         */
        public Builder withContingencyContext(ContingencyContext contingencyContext) {
            this.contingencyContext = Objects.requireNonNull(contingencyContext);
            return this;
        }

        /**
         * <p>Define criteria on network elements.</p>
         * <p>By default, the limit scaling is applied on each network element that holds a limit of the good type.</p>
         * <p>This method is not cumulative and clean previous definitions.</p>
         *
         * @param networkElementCriteria criteria on network elements on which the limit scaling is applied.
         * @return the current {@link Builder}
         */
        public Builder withNetworkElementCriteria(NetworkElementCriterion... networkElementCriteria) {
            return withNetworkElementCriteria(List.of(networkElementCriteria));
        }

        /**
         * <p>Define criteria on network elements.</p>
         * <p>By default, the limit scaling is applied on each network element that holds a limit of the good type.</p>
         * <p>This method is not cumulative and clean previous definitions.</p>
         *
         * @param networkElementCriteria criteria on network elements on which the limit scaling is applied.
         * @return the current {@link Builder}
         */
        public Builder withNetworkElementCriteria(List<NetworkElementCriterion> networkElementCriteria) {
            this.networkElementCriteria = ImmutableList.copyOf(Objects.requireNonNull(networkElementCriteria));
            return this;
        }

        /**
         * <p>Define criteria on permanent limit and/or on acceptable durations of temporary limits within a specific range.</p>
         * <p>By default, the limit scaling is applied for all permanent and temporary limits of the good type.</p>
         * <p>This method is not cumulative and clean previous definitions.</p>
         *
         * @param limitDurationCriteria criteria to restrict the limit scaling to specific durations.
         * @return the current {@link Builder}
         */
        public Builder withLimitDurationCriteria(LimitDurationCriterion... limitDurationCriteria) {
            return withLimitDurationCriteria(List.of(limitDurationCriteria));
        }

        /**
         * <p>Define criteria on permanent limit and/or on acceptable durations of temporary limits within a specific range.</p>
         * <p>By default, the limit scaling is applied for all permanent and temporary limits of the good type.</p>
         * <p>This method is not cumulative and clean previous definitions.</p>
         *
         * @param limitDurationCriteria criteria to restrict the limit scaling to specific durations.
         * @return the current {@link Builder}
         */
        public Builder withLimitDurationCriteria(List<LimitDurationCriterion> limitDurationCriteria) {
            this.limitDurationCriteria = ImmutableList.copyOf(Objects.requireNonNull(limitDurationCriteria));
            return this;
        }

        /**
         * <p>Define the IDs of the {@link com.powsybl.iidm.network.OperationalLimitsGroup} that this scaling should apply to.</p>
         * <p>By default, the limit scaling is applied to all selected groups. This corresponds to an empty selection.</p>
         * <p>This method is not cumulative and cleans previous definitions.</p>
         * @param operationalLimitsGroupIdsSelection restrict the limit scaling to those specified IDs
         * @return the current {@link Builder}
         */
        public Builder withOperationalLimitsGroupIdSelection(String... operationalLimitsGroupIdsSelection) {
            return withOperationalLimitsGroupIdSelection(List.of(operationalLimitsGroupIdsSelection));
        }

        /**
         * <p>Define the IDs of groups of the {@link com.powsybl.iidm.network.OperationalLimitsGroup} that this scaling should apply to.</p>
         * <p>By default, the limit scaling is applied to all selected groups. This corresponds to an empty selection.</p>
         * <p>This method is not cumulative and cleans previous definitions.</p>
         * @param operationalLimitsGroupIdsSelection restrict the limit scaling to those specified IDs
         * @return the current {@link Builder}
         */
        public Builder withOperationalLimitsGroupIdSelection(List<String> operationalLimitsGroupIdsSelection) {
            this.operationalLimitsGroupIdsSelection = operationalLimitsGroupIdsSelection;
            return this;
        }

        /**
         * <p>Build the {@link LimitScaling} with the defined parameters.</p>
         * @return a new {@link LimitScaling}
         */
        public LimitScaling build() {
            return new LimitScaling(limitType, value, monitoringOnly, contingencyContext,
                    networkElementCriteria, limitDurationCriteria, operationalLimitsGroupIdsSelection);
        }
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public double getValue() {
        return value;
    }

    /**
     * <p>Indicate if the limit scaling is applied only to report limit violations (<code>true</code>),
     * or if also affects the conditions of operator strategies (<code>false</code>).</p>
     *
     * @return <code>true</code> if the limit scaling is applied only for monitoring, <code>false</code> otherwise.
     */
    public boolean isMonitoringOnly() {
        return monitoringOnly;
    }

    /**
     * <p>Indicate the limit scaling contingency context.</p>
     *
     * @return the {@link ContingencyContext} of the limit scaling.
     */
    public ContingencyContext getContingencyContext() {
        return contingencyContext;
    }

    /**
     * <p>Indicate the criteria on network elements candidate for the limit scaling.</p>
     *
     * @return the list of the {@link NetworkElementCriterion} candidate for the limit scaling.
     */
    public List<NetworkElementCriterion> getNetworkElementCriteria() {
        return networkElementCriteria;
    }

    /**
     * <p>Indicate criteria on operational limit acceptable durations.</p>
     *
     * @return the list of the {@link LimitDurationCriterion} of the limit scaling.
     */
    public List<LimitDurationCriterion> getDurationCriteria() {
        return durationCriteria;
    }

    /**
     * <p>Indicate the criteria on operational limits group ids than we want to scale</p>
     * @return the list of {@link String} corresponding to the ID of each group that this limit scaling should be applied to.
     */
    public List<String> getOperationalLimitsGroupIdsSelection() {
        return operationalLimitsGroupIdsSelection;
    }
}
