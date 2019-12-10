/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShuntCompensatorAdderAdapter extends AbstractInjectionAdderAdapter<ShuntCompensatorAdder> implements ShuntCompensatorAdder {

    ShuntCompensatorAdderAdapter(final ShuntCompensatorAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public ShuntCompensator add() {
        checkAndSetUniqueId();
        return getIndex().getShuntCompensator(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ShuntCompensatorAdder setbPerSection(final double bPerSection) {
        getDelegate().setbPerSection(bPerSection);
        return this;
    }

    @Override
    public ShuntCompensatorAdder setMaximumSectionCount(final int maximumSectionCount) {
        getDelegate().setMaximumSectionCount(maximumSectionCount);
        return this;
    }

    @Override
    public ShuntCompensatorAdder setCurrentSectionCount(final int currentSectionCount) {
        getDelegate().setCurrentSectionCount(currentSectionCount);
        return this;
    }
}
