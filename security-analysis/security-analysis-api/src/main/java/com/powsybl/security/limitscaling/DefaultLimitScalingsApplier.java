/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling;

import com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapter;
import com.powsybl.iidm.criteria.translation.NetworkElement;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.limitscaling.computation.AbstractLimitsScaler;
import com.powsybl.security.limitscaling.computation.AbstractLimitsScalerCreator;
import com.powsybl.security.limitscaling.computation.DefaultLimitsScaler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Implementation of {@link AbstractLimitScalingsApplier} working with {@link com.powsybl.iidm.network.Identifiable}.</p>
 * <p>You can retrieve the reduced limits by using the {@link #computeLimits(Object, LimitType, ThreeSides, boolean)} method
 * (with an {@link Identifiable} as first parameter).
 * It returns a {@link com.powsybl.iidm.network.limitmodification.result.LimitsContainer} containing both
 * the original limits (accessible via {@link LimitsContainer#getOriginalLimits()}) and the reduced limits
 * (accessible via {@link LimitsContainer#getLimits()}).</p>
 * <p>Since LimitReductions depend on the contingency context, you should call {@link #setWorkingContingency(String contingencyId)}
 * each time the studied contingency change (use <code>null</code> for pre-contingency state).</p>
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultLimitScalingsApplier extends AbstractLimitScalingsApplier<Identifiable<?>, LoadingLimits> {

    private final Map<Identifiable<?>, DefaultNetworkElementAdapter> networkElementAdapterCache = new HashMap<>();

    /**
     * Create a new {@link AbstractLimitScalingsApplier} for {@link com.powsybl.iidm.network.Identifiable}
     * using a list of reductions.
     *
     * @param limitScalingList the list of the reductions to use when computing reduced limits.
     */
    public DefaultLimitScalingsApplier(List<LimitScaling> limitScalingList) {
        super(limitScalingList);
    }

    @Override
    protected OriginalLimitsGetter<Identifiable<?>, LoadingLimits> getOriginalLimitsGetter() {
        return (identifiable, limitType, side) -> {
            HashMap<String, LoadingLimits> limitsByGroupId = new HashMap<>();
            LimitViolationUtils.getAllSelectedLimitsGroups(identifiable, side)
                .forEach(group ->
                    group.getLoadingLimits(limitType).ifPresent(l -> limitsByGroupId.put(group.getId(), l)));
            return limitsByGroupId;
        };
    }

    @Override
    protected AbstractLimitsScalerCreator<LoadingLimits, AbstractLimitsScaler<LoadingLimits>> getLimitsReducerCreator() {
        return (networkElementId, limitsGroupId, originalLimits) -> new DefaultLimitsScaler(originalLimits, limitsGroupId);
    }

    @Override
    protected NetworkElement asNetworkElement(Identifiable<?> identifiable) {
        return networkElementAdapterCache.computeIfAbsent(identifiable, id -> new DefaultNetworkElementAdapter(identifiable));
    }
}
