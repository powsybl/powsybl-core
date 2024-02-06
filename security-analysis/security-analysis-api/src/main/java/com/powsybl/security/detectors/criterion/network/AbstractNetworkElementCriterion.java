/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors.criterion.network;

import java.util.Set;

public abstract class AbstractNetworkElementCriterion {

    public enum NetworkElementCriterionType {
        LINE,
        TWO_WINDING_TRANSFORMER,
        THREE_WINDING_TRANSFORMER
    }

    private final Set<String> networkElementIds;

    protected AbstractNetworkElementCriterion(Set<String> networkElementIds) {
        this.networkElementIds = networkElementIds;
    }

    public Set<String> getNetworkElementIds() {
        return networkElementIds;
    }

    public abstract NetworkElementCriterionType getNetworkElementCriterionType();

    public abstract boolean accept(NetworkElementVisitor networkElementVisitor);

}
