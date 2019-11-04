/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ShuntCompensatorAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShuntCompensatorAdderAdapter extends AbstractAdapter<ShuntCompensatorAdder> implements ShuntCompensatorAdder {

    protected ShuntCompensatorAdderAdapter(final ShuntCompensatorAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ShuntCompensatorAdderAdapter setNode(final int node) {
        getDelegate().setNode(node);
        return this;
    }

    @Override
    public ShuntCompensatorAdderAdapter setBus(final String bus) {
        getDelegate().setBus(bus);
        return this;
    }

    @Override
    public ShuntCompensatorAdderAdapter setConnectableBus(final String connectableBus) {
        getDelegate().setConnectableBus(connectableBus);
        return this;
    }

    @Override
    public ShuntCompensatorAdderAdapter setId(final String id) {
        getDelegate().setId(id);
        return this;
    }

    @Override
    public ShuntCompensatorAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        getDelegate().setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public ShuntCompensatorAdderAdapter setName(final String name) {
        getDelegate().setName(name);
        return this;
    }

    @Override
    public ShuntCompensatorAdderAdapter setbPerSection(final double bPerSection) {
        getDelegate().setbPerSection(bPerSection);
        return this;
    }

    @Override
    public ShuntCompensatorAdderAdapter setMaximumSectionCount(final int maximumSectionCount) {
        getDelegate().setMaximumSectionCount(maximumSectionCount);
        return this;
    }

    @Override
    public ShuntCompensatorAdderAdapter setCurrentSectionCount(final int currentSectionCount) {
        getDelegate().setCurrentSectionCount(currentSectionCount);
        return this;
    }

    @Override
    public ShuntCompensatorAdapter add() {
        return new ShuntCompensatorAdapter(getDelegate().add(), getIndex());
    }
}
