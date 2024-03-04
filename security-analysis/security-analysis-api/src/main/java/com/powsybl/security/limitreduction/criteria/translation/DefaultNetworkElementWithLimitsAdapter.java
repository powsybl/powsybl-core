/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction.criteria.translation;

import com.powsybl.iidm.criteria.translation.DefaultNetworkElementAdapter;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.limitreduction.ContingencyWiseReducedLimitsComputer;
import com.powsybl.security.limitreduction.ReducedLimitsComputer;

import java.util.Optional;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultNetworkElementWithLimitsAdapter extends DefaultNetworkElementAdapter implements ContingencyWiseReducedLimitsComputer.FilterableNetworkElement {
    public static final ReducedLimitsComputer.OriginalLimitsGetter<ContingencyWiseReducedLimitsComputer.FilterableNetworkElement, LoadingLimits> IDENTIFIABLE_LIMITS_GETTER = new IdentifiableLimitsGetter();

    public DefaultNetworkElementWithLimitsAdapter(Identifiable<?> identifiable) {
        super(identifiable);
    }

    public static ReducedLimitsComputer.OriginalLimitsGetter<ContingencyWiseReducedLimitsComputer.FilterableNetworkElement, LoadingLimits> getOriginalLimitsGetterForIdentifiables() {
        return IDENTIFIABLE_LIMITS_GETTER;
    }

    static class IdentifiableLimitsGetter implements ReducedLimitsComputer.OriginalLimitsGetter<ContingencyWiseReducedLimitsComputer.FilterableNetworkElement, LoadingLimits> {
        @Override
        public Optional<LoadingLimits> getLimits(ContingencyWiseReducedLimitsComputer.FilterableNetworkElement filterable, LimitType limitType, ThreeSides side) {
            Identifiable<?> identifiable = ((DefaultNetworkElementWithLimitsAdapter) filterable).getIdentifiable();
            return LimitViolationUtils.getLoadingLimits(identifiable, limitType, side);
        }
    }
}
