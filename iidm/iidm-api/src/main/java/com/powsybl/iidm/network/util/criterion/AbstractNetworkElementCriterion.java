/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion;

public abstract class AbstractNetworkElementCriterion {

    public enum NetworkElementCriterionType {
        LINE,
        TWO_WINDING_TRANSFORMER,
        THREE_WINDING_TRANSFORMER,
        IDENTIFIERS
    }

    public abstract NetworkElementCriterionType getNetworkElementCriterionType();

    public abstract boolean accept(NetworkElementVisitor networkElementVisitor);
}
