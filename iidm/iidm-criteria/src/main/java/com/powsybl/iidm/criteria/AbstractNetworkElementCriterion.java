/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

/**
 * <p>Abstract classes for {@link NetworkElementCriterion} implementations.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractNetworkElementCriterion implements NetworkElementCriterion {
    private final String name;

    protected AbstractNetworkElementCriterion(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
