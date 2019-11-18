/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel.TopologyTraverser;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TerminalAdapter extends AbstractAdapter<Terminal> implements Terminal {

    protected TerminalAdapter(final Terminal delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public VoltageLevelAdapter getVoltageLevel() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TerminalBusBreakerViewAdapter getBusBreakerView() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TerminalBusViewAdapter getBusView() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Connectable getConnectable() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getP() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TerminalAdapter setP(final double p) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getQ() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TerminalAdapter setQ(final double q) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getI() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean connect() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean disconnect() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean isConnected() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void traverse(final TopologyTraverser traverser) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
