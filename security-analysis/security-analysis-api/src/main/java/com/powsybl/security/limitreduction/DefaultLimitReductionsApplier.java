/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapter;
import com.powsybl.iidm.criteria.translation.NetworkElement;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.limitreduction.computation.AbstractLimitsReducer;
import com.powsybl.security.limitreduction.computation.AbstractLimitsReducerCreator;
import com.powsybl.security.limitreduction.computation.DefaultLimitsReducer;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Implementation of {@link AbstractLimitReductionsApplier} working with {@link com.powsybl.iidm.network.Identifiable}.</p>
 * <p>You can retrieve the reduced limits by using the {@link #computeLimits(Object, LimitType, ThreeSides)} method
 * (with an {@link Identifiable} as first parameter).
 * It returns a {@link com.powsybl.iidm.network.limitmodification.result.LimitsContainer} containing both
 * the original limits (accessible via {@link LimitsContainer#getOriginalLimits()}) and the reduced limits
 * (accessible via {@link LimitsContainer#getLimits()}).</p>
 * <p>Since LimitReductions depend on the contingency context, you should call {@link #changeContingencyId(String contingencyId)}
 * each time the studied contingency change (use <code>null</code> for pre-contingency state).</p>
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultLimitReductionsApplier extends AbstractLimitReductionsApplier<Identifiable<?>, LoadingLimits> {

    private final Map<Identifiable<?>, DefaultNetworkElementAdapter> networkElementAdapterCache = new HashMap<>();

    /**
     * Create a new {@link AbstractLimitReductionsApplier} for {@link com.powsybl.iidm.network.Identifiable}
     * using a list of reductions.
     *
     * @param limitReductionList the list of the reductions to use when computing reduced limits.
     */
    public DefaultLimitReductionsApplier(LimitReductionList limitReductionList) {
        super(limitReductionList);
    }

    @Override
    protected OriginalLimitsGetter<Identifiable<?>, LoadingLimits> getOriginalLimitsGetter() {
        return LimitViolationUtils::getLoadingLimits;
    }

    @Override
    protected AbstractLimitsReducerCreator<LoadingLimits, AbstractLimitsReducer<LoadingLimits>> getLimitsReducerCreator() {
        return (id, originalLimits) -> new DefaultLimitsReducer(originalLimits);
    }

    @Override
    protected NetworkElement asNetworkElement(Identifiable<?> identifiable) {
        return networkElementAdapterCache.computeIfAbsent(identifiable, id -> new DefaultNetworkElementAdapter(identifiable));
    }
}
