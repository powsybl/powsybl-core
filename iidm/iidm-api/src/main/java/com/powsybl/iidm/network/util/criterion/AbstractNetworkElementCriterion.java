/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion;

import java.util.Set;

public abstract class AbstractNetworkElementCriterion<T> {

    public enum NetworkElementCriterionType {
        LINE,
        TWO_WINDING_TRANSFORMER,
        THREE_WINDING_TRANSFORMER
    }

    private Set<String> networkElementIds = Set.of();

    public T setNetworkElementIds(Set<String> networkElementIds) {
        this.networkElementIds = networkElementIds;
        return (T) this;
    }

    public Set<String> getNetworkElementIds() {
        return networkElementIds;
    }

    public abstract NetworkElementCriterionType getNetworkElementCriterionType();

    public abstract boolean accept(NetworkElementVisitor networkElementVisitor);

}
