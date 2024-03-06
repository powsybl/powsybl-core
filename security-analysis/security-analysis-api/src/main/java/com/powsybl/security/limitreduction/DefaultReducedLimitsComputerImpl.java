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
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link AbstractContingencyWiseReducedLimitsComputer} working with {@link com.powsybl.iidm.network.Identifiable}.
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultReducedLimitsComputerImpl extends AbstractContingencyWiseReducedLimitsComputer<Identifiable<?>, LoadingLimits> {

    private final Map<Identifiable<?>, DefaultNetworkElementAdapter> networkElementAdapterCache = new HashMap<>();

    /**
     * Create a new {@link AbstractContingencyWiseReducedLimitsComputer} for {@link com.powsybl.iidm.network.Identifiable}
     * using a list of reduction definitions.
     *
     * @param limitReductionDefinitionList the list of the reduction definitions to use when computing reduced limits.
     */
    public DefaultReducedLimitsComputerImpl(LimitReductionDefinitionList limitReductionDefinitionList) {
        super(limitReductionDefinitionList);
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
