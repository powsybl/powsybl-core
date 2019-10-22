/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.List;

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

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public TerminalAdapter getTerminal() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ConnectableType getType() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LoadType getLoadType() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LoadAdapter setLoadType(final LoadType loadType) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getP0() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LoadAdapter setP0(final double p0) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getQ0() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public LoadAdapter setQ0(final double q0) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
