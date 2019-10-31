/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.List;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.ShuntCompensator;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShuntCompensatorAdapter extends AbstractIdentifiableAdapter<ShuntCompensator> implements ShuntCompensator {

    protected ShuntCompensatorAdapter(final ShuntCompensator delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        return getDelegate().getTerminals().stream()
            .map(getIndex()::getTerminal)
            .collect(Collectors.toList());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public TerminalAdapter getTerminal() {
        return getIndex().getTerminal(getDelegate().getTerminal());
    }

    @Override
    public ConnectableType getType() {
        return getDelegate().getType();
    }

    @Override
    public int getMaximumSectionCount() {
        return getDelegate().getMaximumSectionCount();
    }

    @Override
    public ShuntCompensatorAdapter setMaximumSectionCount(final int maximumSectionCount) {
        getDelegate().setMaximumSectionCount(maximumSectionCount);
        return this;
    }

    @Override
    public int getCurrentSectionCount() {
        return getDelegate().getCurrentSectionCount();
    }

    @Override
    public ShuntCompensatorAdapter setCurrentSectionCount(final int currentSectionCount) {
        getDelegate().setCurrentSectionCount(currentSectionCount);
        return this;
    }

    @Override
    public double getbPerSection() {
        return getDelegate().getbPerSection();
    }

    @Override
    public ShuntCompensatorAdapter setbPerSection(final double bPerSection) {
        getDelegate().setbPerSection(bPerSection);
        return this;
    }

    @Override
    public double getMaximumB() {
        return getDelegate().getMaximumB();
    }

    @Override
    public double getCurrentB() {
        return getDelegate().getCurrentB();
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
