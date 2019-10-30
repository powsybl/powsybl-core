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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadType;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LoadAdapter extends AbstractIdentifiableAdapter<Load> implements Load {

    protected LoadAdapter(final Load delegate, final MergingViewIndex index) {
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
    public double getP0() {
        return getDelegate().getP0();
    }

    @Override
    public LoadAdapter setP0(final double p0) {
        getDelegate().setP0(p0);
        return this;
    }

    @Override
    public double getQ0() {
        return getDelegate().getQ0();
    }

    @Override
    public LoadAdapter setQ0(final double q0) {
        getDelegate().setQ0(q0);
        return this;
    }

    @Override
    public LoadType getLoadType() {
        return getDelegate().getLoadType();
    }

    @Override
    public LoadAdapter setLoadType(final LoadType loadType) {
        getDelegate().setLoadType(loadType);
        return this;
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
