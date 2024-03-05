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
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.security.limitreduction.AbstractContingencyWiseReducedLimitsComputer;
import com.powsybl.security.limitreduction.ReducedLimitsComputer;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultNetworkElementIdentifiableAdapter extends DefaultNetworkElementAdapter {
    //TODO To remove

    //public static final ReducedLimitsComputer.OriginalLimitsGetter<AbstractContingencyWiseReducedLimitsComputer.ProcessableNetworkElement, LoadingLimits> IDENTIFIABLE_LIMITS_GETTER = new IdentifiableLimitsGetter();
    public static final ReducedLimitsComputer.OriginalLimitsGetter<AbstractContingencyWiseReducedLimitsComputer.ProcessableNetworkElement, LoadingLimits> IDENTIFIABLE_LIMITS_GETTER = null;

    public DefaultNetworkElementIdentifiableAdapter(Identifiable<?> identifiable) {
        super(identifiable);
    }

    public static ReducedLimitsComputer.OriginalLimitsGetter<AbstractContingencyWiseReducedLimitsComputer.ProcessableNetworkElement, LoadingLimits> getOriginalLimitsGetterForIdentifiables() {
        return IDENTIFIABLE_LIMITS_GETTER;
    }
}
