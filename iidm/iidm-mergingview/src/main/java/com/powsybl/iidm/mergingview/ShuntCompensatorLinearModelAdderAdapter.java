/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.ShuntCompensatorLinearModelAdder;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ShuntCompensatorLinearModelAdderAdapter implements ShuntCompensatorLinearModelAdder {

    private final ShuntCompensatorLinearModelAdder delegate;
    private final ShuntCompensatorAdderAdapter parent;

    ShuntCompensatorLinearModelAdderAdapter(final ShuntCompensatorLinearModelAdder delegate, ShuntCompensatorAdderAdapter parent) {
        this.delegate = Objects.requireNonNull(delegate);
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public ShuntCompensatorLinearModelAdder setbPerSection(double bPerSection) {
        delegate.setbPerSection(bPerSection);
        return this;
    }

    @Override
    public ShuntCompensatorLinearModelAdder setgPerSection(double gPerSection) {
        delegate.setgPerSection(gPerSection);
        return this;
    }

    @Override
    public ShuntCompensatorLinearModelAdder setMaximumSectionCount(int maximumSectionCount) {
        delegate.setMaximumSectionCount(maximumSectionCount);
        return this;
    }

    @Override
    public ShuntCompensatorAdder add() {
        delegate.add();
        return parent;
    }
}
