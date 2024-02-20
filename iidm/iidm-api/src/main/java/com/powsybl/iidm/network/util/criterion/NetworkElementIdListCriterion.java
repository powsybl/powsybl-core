/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion;

import java.util.Set;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class NetworkElementIdListCriterion extends AbstractNetworkElementCriterion {
    public static final NetworkElementCriterionType TYPE = NetworkElementCriterionType.IDENTIFIERS;

    private final Set<String> networkElementIds;

    public NetworkElementIdListCriterion(Set<String> networkElementIds) {
        this(null, networkElementIds);
    }

    public NetworkElementIdListCriterion(String name, Set<String> networkElementIds) {
        super(name);
        this.networkElementIds = networkElementIds;
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return TYPE;
    }

    @Override
    public boolean accept(NetworkElementVisitor networkElementVisitor) {
        return networkElementVisitor.visit(this);
    }

    public Set<String> getNetworkElementIds() {
        return networkElementIds;
    }
}
