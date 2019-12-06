/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ShuntCompensator;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShuntCompensatorAdapter extends AbstractInjectionAdapter<ShuntCompensator> implements ShuntCompensator {

    ShuntCompensatorAdapter(final ShuntCompensator delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public int getMaximumSectionCount() {
        return getDelegate().getMaximumSectionCount();
    }

    @Override
    public ShuntCompensator setMaximumSectionCount(final int maximumSectionCount) {
        getDelegate().setMaximumSectionCount(maximumSectionCount);
        return this;
    }

    @Override
    public int getCurrentSectionCount() {
        return getDelegate().getCurrentSectionCount();
    }

    @Override
    public ShuntCompensator setCurrentSectionCount(final int currentSectionCount) {
        getDelegate().setCurrentSectionCount(currentSectionCount);
        return this;
    }

    @Override
    public double getbPerSection() {
        return getDelegate().getbPerSection();
    }

    @Override
    public ShuntCompensator setbPerSection(final double bPerSection) {
        getDelegate().setbPerSection(bPerSection);
        return this;
    }

    @Override
    public double getCurrentB() {
        return getDelegate().getCurrentB();
    }
}
