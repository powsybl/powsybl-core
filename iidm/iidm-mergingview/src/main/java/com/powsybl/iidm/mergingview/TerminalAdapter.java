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

    private TerminalBusBreakerViewAdapter busBreakerView;
    private TerminalBusViewAdapter busView;
    private TerminalNodeBreakerViewAdapter nodeBreakerView;

    protected TerminalAdapter(final Terminal delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public VoltageLevelAdapter getVoltageLevel() {
        return getIndex().getVoltageLevel(getDelegate().getVoltageLevel());
    }

    @Override
    public TerminalNodeBreakerViewAdapter getNodeBreakerView() {
        if (nodeBreakerView == null) {
            nodeBreakerView = new TerminalNodeBreakerViewAdapter(getDelegate().getNodeBreakerView(), getIndex());
        }
        return nodeBreakerView;
    }

    @Override
    public TerminalBusBreakerViewAdapter getBusBreakerView() {
        if (busBreakerView == null) {
            busBreakerView = new TerminalBusBreakerViewAdapter(getDelegate().getBusBreakerView(), getIndex());
        }
        return busBreakerView;
    }

    @Override
    public TerminalBusViewAdapter getBusView() {
        if (busView == null) {
            busView = new TerminalBusViewAdapter(getDelegate().getBusView(), getIndex());
        }
        return busView;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public Connectable getConnectable() {
        return getIndex().getConnectable(getDelegate().getConnectable());
    }

    @Override
    public double getP() {
        return getDelegate().getP();
    }

    @Override
    public TerminalAdapter setP(final double p) {
        getDelegate().setP(p);
        return this;
    }

    @Override
    public double getQ() {
        return getDelegate().getQ();
    }

    @Override
    public TerminalAdapter setQ(final double q) {
        getDelegate().setQ(q);
        return this;
    }

    @Override
    public double getI() {
        return getDelegate().getI();
    }

    @Override
    public boolean connect() {
        return getDelegate().connect();
    }

    @Override
    public boolean disconnect() {
        return getDelegate().disconnect();
    }

    @Override
    public boolean isConnected() {
        return getDelegate().isConnected();
    }

    @Override
    public void traverse(final TopologyTraverser traverser) {
        getDelegate().traverse(traverser);
    }
}
